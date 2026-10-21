package mezz.jei.gui.input;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.common.input.IGuiInputLayer;
import mezz.jei.common.input.IMouseOverable;
import mezz.jei.common.input.UserInput;
import mezz.jei.common.input.interaction.IInputInteraction;
import mezz.jei.gui.input.focus.GuiKeyboardFocus;
import net.minecraft.client.gui.screens.Screen;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Lists input areas ({@link InputArea}) from front to back. Keyboard events target the focused
 * area when one exists; pointer events target the first area under the mouse.
 * Input handling and ingredient lookup share that selection for the duration of one event.
 *
 * <p>For example, without keyboard focus, a player hovers over cobblestone in a recipe tooltip that covers an
 * iron ingot in the ingredient list. Pressing the Show Recipes key looks up cobblestone,
 * because the tooltip is in front of the list.</p>
 */
public final class GuiInputScene {
	/** Input areas from front to back; also the order for trying shortcuts outside the selected input area. */
	private final List<InputArea> inputAreas;
	private final GuiKeyboardFocus keyboardFocus;
	/** Input area chosen for the event being handled; null when no event is being handled. */
	private @Nullable HitTest dispatchTarget;

	public GuiInputScene(GuiKeyboardFocus keyboardFocus, InputArea... inputAreas) {
		this.keyboardFocus = keyboardFocus;
		this.inputAreas = List.of(inputAreas);
	}

	public List<InputArea> getInputAreas() {
		return inputAreas;
	}

	public <T> T dispatch(double mouseX, double mouseY, Supplier<T> action) {
		keyboardFocus.update();
		return dispatch(findTarget(mouseX, mouseY).orElse(null), action);
	}

	public <T> T dispatch(UserInput input, Supplier<T> action) {
		keyboardFocus.update();
		return dispatch(findTarget(input).orElse(null), action);
	}

	private Optional<InputArea> findTarget(UserInput input) {
		if (input.isKeyboardInput()) {
			Optional<InputArea> keyboardTarget = getKeyboardTarget();
			if (keyboardTarget.isPresent()) {
				return keyboardTarget;
			}
		}
		return findTarget(input.getMouseX(), input.getMouseY());
	}

	public Optional<InputArea> getTarget(UserInput input) {
		if (dispatchTarget != null) {
			return Optional.ofNullable(dispatchTarget.inputArea());
		}
		return findTarget(input);
	}

	public Optional<InputArea> getKeyboardTarget() {
		return inputAreas.stream()
			.filter(area -> area.keyboardNavigation() != null && keyboardFocus.isFocused(area.keyboardNavigation()))
			.findFirst();
	}

	public void clearKeyboardFocus() {
		inputAreas.forEach(area -> {
			if (area.keyboardNavigation() != null) {
				keyboardFocus.clear(area.keyboardNavigation());
			}
		});
	}

	/** Clears focus whose screen or focused component is no longer available. */
	public void updateKeyboardFocus() {
		keyboardFocus.update();
	}

	private <T> T dispatch(@Nullable InputArea target, Supplier<T> action) {
		HitTest previousTarget = this.dispatchTarget;
		this.dispatchTarget = new HitTest(target);
		try {
			return action.get();
		} finally {
			this.dispatchTarget = previousTarget;
			keyboardFocus.update();
		}
	}

	public Optional<InputArea> getTarget(double mouseX, double mouseY) {
		if (dispatchTarget != null) {
			return Optional.ofNullable(dispatchTarget.inputArea());
		}
		return findTarget(mouseX, mouseY);
	}

	private Optional<InputArea> findTarget(double mouseX, double mouseY) {
		return inputAreas.stream()
			.filter(inputArea -> inputArea.area().isMouseOver(mouseX, mouseY))
			.findFirst();
	}

	public boolean isTarget(IMouseOverable area, UserInput input) {
		return getTarget(input)
			.map(inputArea -> inputArea.area() == area)
			.orElse(false);
	}

	Stream<IRecipeFocusSource> getFocusSources(double mouseX, double mouseY) {
		return getTarget(mouseX, mouseY).stream()
			.map(InputArea::area)
			.filter(IRecipeFocusSource.class::isInstance)
			.map(IRecipeFocusSource.class::cast);
	}

	public List<IGuiInputLayer> getForegroundLayers() {
		return inputAreas.stream()
			.map(InputArea::area)
			.filter(IGuiInputLayer.class::isInstance)
			.map(IGuiInputLayer.class::cast)
			.toList();
	}

	Optional<IInputInteraction> startDrag(Screen screen, UserInput input) {
		InputArea target = getTarget(input).orElse(null);
		if (target != null) {
			for (DragSource source : target.dragSources()) {
				Optional<IInputInteraction> drag = source.begin(screen, input);
				if (drag.isPresent()) {
					return drag;
				}
			}
		}
		return Optional.empty();
	}

	/** Public recipe drag callbacks use the current pointer position, including when no control accepted the press. */
	boolean dragApiHandlers(double mouseX, double mouseY, InputConstants.Key button, double dragX, double dragY) {
		return dispatch(mouseX, mouseY, () -> {
			InputArea target = getTarget(mouseX, mouseY).orElse(null);
			if (target == null) {
				return false;
			}
			return (target.apiDragHandler() != null && target.apiDragHandler().handleMouseDragged(mouseX, mouseY, button, dragX, dragY)) ||
				target.blocksUnhandledMouseInput();
		});
	}

	void resetInput() {
		keyboardFocus.clear();
		inputAreas.forEach(inputArea -> inputArea.controls().resetInput());
	}

	/**
	 * The input area selected for one event.
	 *
	 * @param inputArea the selected input area, or null if the event has no target
	 */
	private record HitTest(@Nullable InputArea inputArea) {
	}
}
