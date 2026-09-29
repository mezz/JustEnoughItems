package mezz.jei.gui.input;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.common.config.DebugConfig;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.input.UserInput;
import mezz.jei.common.input.interaction.ConsumedInput;
import mezz.jei.common.input.interaction.IInputInteraction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Sends input to JEI controls and shortcuts, keeping mouse interactions until release or cancellation.
 * Focused text editing gets the first chance to handle keyboard input. Controls under
 * the pointer are tried before shortcuts. Shortcut priority is the selected input area's
 * shortcuts, then shared shortcuts, then other areas' shortcuts that work anywhere on the screen.
 *
 * <p>For example, a player editing "stone" presses Backspace. The search field removes
 * the "e", even if Backspace is also bound to show or hide JEI.</p>
 */
public final class GuiInputController {
	private static final Logger LOGGER = LogManager.getLogger();
	/** Input areas from front to back, shared with ingredient lookup. */
	private final GuiInputScene scene;
	/** Shortcuts shared by input areas, such as looking up recipes and showing or hiding overlays. */
	private final InputCommands commands;
	private final ITextInputHandler editor;
	private final IInternalKeyMappings keys;
	/** Interactions and original screens for mouse buttons that are still held. */
	private final Map<InputConstants.Key, MouseCapture> captures = new HashMap<>();
	/** Detects screen changes even when a handler returns to the same screen before finishing. */
	private int screenChangeCount;

	public GuiInputController(GuiInputScene scene, InputCommands commands, ITextInputHandler editor, IInternalKeyMappings keys) {
		this.scene = scene;
		this.commands = commands;
		this.editor = editor;
		this.keys = keys;
	}

	public boolean keyPressed(Screen screen, UserInput input) {
		return scene.dispatch(input.getMouseX(), input.getMouseY(), () -> {
			int initialScreenChangeCount = screenChangeCount;
			Optional<IInputInteraction> action = resolve(screen, input);
			if (!isScreenUnchanged(screen, initialScreenChangeCount)) {
				action.ifPresent(IInputInteraction::cancel);
				return true;
			}
			action.ifPresent(interaction -> interaction.complete(input));
			return action.isPresent();
		});
	}

	public boolean mousePressed(Screen screen, UserInput input) {
		MouseCapture previous = captures.get(input.getKey());
		if (previous != null && previous.keepsCaptureOnRepeatedPress()) {
			return true;
		}
		cancel(input.getKey());
		return scene.dispatch(input.getMouseX(), input.getMouseY(), () -> {
			InputArea target = scene.getTarget(input.getMouseX(), input.getMouseY()).orElse(null);
			int initialScreenChangeCount = screenChangeCount;
			IInputInteraction interaction = resolve(screen, input).orElse(null);
			IInputInteraction ingredientDrag = null;
			if (isScreenUnchanged(screen, initialScreenChangeCount) && input.is(keys.getLeftClick())) {
				ingredientDrag = scene.startDrag(screen, input).orElse(null);
			}
			if (interaction == null && ingredientDrag == null && target != null && target.blocksUnhandledMouseInput()) {
				interaction = ConsumedInput.INSTANCE;
			}
			if (interaction == null && ingredientDrag == null) {
				return false;
			}
			MouseCapture capture = new MouseCapture(screen, interaction, ingredientDrag);
			if (isScreenUnchanged(screen, initialScreenChangeCount)) {
				captures.put(input.getKey(), capture);
			} else {
				capture.cancel();
			}
			return true;
		});
	}

	private Optional<IInputInteraction> resolve(Screen screen, UserInput input) {
		InputArea target = scene.getTarget(input.getMouseX(), input.getMouseY()).orElse(null);
		if (DebugConfig.isDebugInputsEnabled()) {
			String targetName = "none";
			if (target != null) {
				targetName = target.name();
			}
			LOGGER.debug("JEI input {} targeting {}", input, targetName);
		}
		boolean editing = input.isKeyboardInput() && editor.hasKeyboardFocus();
		if (editing) {
			Optional<IInputInteraction> edit = editor.beginInput(screen, input, keys);
			if (edit.isPresent()) {
				return edit;
			}
		}
		if (target != null) {
			if (!editing || target.area() != editor) {
				Optional<IInputInteraction> control = target.controls().beginInput(screen, input, keys);
				if (control.isPresent()) {
					return control;
				}
			}
			Optional<IInputInteraction> shortcut = target.commands().resolve(screen, input, true);
			if (shortcut.isPresent()) {
				return shortcut;
			}
		}
		Optional<IInputInteraction> global = commands.resolve(screen, input, false);
		if (global.isPresent()) {
			return global;
		}
		for (InputArea inputArea : scene.getInputAreas()) {
			if (inputArea != target) {
				Optional<IInputInteraction> shortcut = inputArea.commands().resolve(screen, input, false);
				if (shortcut.isPresent()) {
					return shortcut;
				}
			}
		}
		return Optional.empty();
	}

	public boolean mouseReleased(Screen screen, UserInput input) {
		MouseCapture capture = captures.remove(input.getKey());
		if (capture != null && capture.screen() != screen) {
			capture.cancel();
			return true;
		}
		InputArea target = scene.getTarget(input.getMouseX(), input.getMouseY()).orElse(null);
		if (target == null || target.area() != editor) {
			editor.clearFocus();
		}
		if (capture == null) {
			return false;
		}
		int initialScreenChangeCount = screenChangeCount;
		if (capture.interaction() != null) {
			capture.interaction().complete(input);
		}
		if (capture.ingredientDrag() != null) {
			if (isScreenUnchanged(screen, initialScreenChangeCount)) {
				capture.ingredientDrag().complete(input);
			} else {
				capture.ingredientDrag().cancel();
			}
		}
		return true;
	}

	public boolean mouseDragged(Screen screen, double mouseX, double mouseY, InputConstants.Key button, double dragX, double dragY) {
		MouseCapture capture = captures.get(button);
		if (capture == null) {
			return scene.dragApiHandlers(mouseX, mouseY, button, dragX, dragY);
		}
		if (capture.screen() != screen) {
			cancel(button);
			return true;
		}
		int initialScreenChangeCount = screenChangeCount;
		if (capture.interaction() != null) {
			capture.interaction().drag(mouseX, mouseY, button, dragX, dragY);
		}
		if (capture.ingredientDrag() != null && isScreenUnchanged(screen, initialScreenChangeCount)) {
			capture.ingredientDrag().drag(mouseX, mouseY, button, dragX, dragY);
		}
		if (isScreenUnchanged(screen, initialScreenChangeCount) && !capture.capturesMouseMovement()) {
			scene.dragApiHandlers(mouseX, mouseY, button, dragX, dragY);
		}
		return true;
	}

	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		return scene.dispatch(mouseX, mouseY, () -> {
			InputArea target = scene.getTarget(mouseX, mouseY).orElse(null);
			if (target == null) {
				return false;
			}
			return target.controls().scroll(mouseX, mouseY, scrollX, scrollY) || target.blocksUnhandledMouseInput();
		});
	}

	public boolean charTyped(CharacterEvent event) {
		return editor.hasKeyboardFocus() && editor.onCharTyped(event);
	}

	public void screenChanged() {
		screenChangeCount++;
		List<MouseCapture> pending = List.copyOf(captures.values());
		captures.clear();
		pending.forEach(MouseCapture::cancel);
		editor.clearFocus();
		scene.resetInput();
	}

	private boolean isScreenUnchanged(Screen screen, int initialScreenChangeCount) {
		return screenChangeCount == initialScreenChangeCount && Minecraft.getInstance().gui.screen() == screen;
	}

	private void cancel(InputConstants.Key button) {
		MouseCapture capture = captures.remove(button);
		if (capture != null) {
			capture.cancel();
		}
	}

	/**
	 * Pending interactions for one mouse button. A press can select both a click action
	 * and an ingredient drag.
	 *
	 * @param screen screen where the press was accepted; a release on another screen cancels it
	 * @param interaction interaction for the selected control or shortcut, or null if only a drag started
	 * @param ingredientDrag interaction for an ingredient drag started by the same press, or null if none started
	 */
	private record MouseCapture(Screen screen, @Nullable IInputInteraction interaction, @Nullable IInputInteraction ingredientDrag) {
		private void cancel() {
			if (interaction != null) {
				interaction.cancel();
			}
			if (ingredientDrag != null) {
				ingredientDrag.cancel();
			}
		}

		private boolean capturesMouseMovement() {
			return (interaction != null && interaction.capturesMouseMovement()) ||
				(ingredientDrag != null && ingredientDrag.capturesMouseMovement());
		}

		private boolean keepsCaptureOnRepeatedPress() {
			return (interaction != null && interaction.keepsCaptureOnRepeatedPress()) || (ingredientDrag != null && ingredientDrag.keepsCaptureOnRepeatedPress());
		}
	}
}
