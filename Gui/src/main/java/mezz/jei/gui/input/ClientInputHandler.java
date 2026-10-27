package mezz.jei.gui.input;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.input.UserInput;
import mezz.jei.common.input.interaction.IInputInteraction;
import mezz.jei.gui.util.GuiEventListenerUtil;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;

import java.util.Optional;

/** Connects Minecraft's input callbacks to JEI's input controller. */
public class ClientInputHandler {
	private final GuiInputController controller;
	private final IInternalKeyMappings keybindings;
	/** Retained for every character callback from the last key press. */
	private Optional<IInputInteraction> keyInteraction = Optional.empty();

	public ClientInputHandler(GuiInputController controller, IInternalKeyMappings keybindings) {
		this.controller = controller;
		this.keybindings = keybindings;
	}

	public void onGuiChanged() {
		keyInteraction = Optional.empty();
		controller.screenChanged();
	}

	/**
	 * Offers key presses to JEI before the Minecraft screen handles them, when no Minecraft
	 * text field has keyboard focus. The Focus Search shortcut and chat also get this early check.
	 *
	 * <p>For example, when a player uses Focus Search in the creative inventory, JEI can
	 * select its search field before the creative inventory's search field handles the key.</p>
	 */
	public boolean onKeyboardKeyPressedPre(Screen screen, UserInput input) {
		keyInteraction = Optional.empty();
		// Focus Search transfers keyboard focus even when the creative inventory's search field is selected.
		if (screen instanceof ChatScreen || input.is(keybindings.getFocusSearch()) || !isContainerTextFieldFocused(screen)) {
			keyInteraction = controller.keyPressed(screen, input);
		}
		return keyInteraction.isPresent();
	}

	/**
	 * Offers a key press to JEI after the Minecraft screen has declined it, when a Minecraft
	 * text field has keyboard focus. This lets that text field handle the key first.
	 */
	public boolean onKeyboardKeyPressedPost(Screen screen, UserInput input) {
		if (!(screen instanceof ChatScreen) && isContainerTextFieldFocused(screen)) {
			keyInteraction = controller.keyPressed(screen, input);
			return keyInteraction.isPresent();
		}
		return false;
	}

	/**
	 * Offers typed characters to JEI before the Minecraft screen handles them, when no
	 * Minecraft text field has keyboard focus. JEI inserts them only if its search field
	 * has keyboard focus. Characters from a consumed action are cancelled before reaching
	 * either JEI's editor or the screen.
	 */
	public boolean onKeyboardCharTypedPre(Screen screen, char codePoint, int modifiers) {
		if (consumesCharacters()) {
			return true;
		}
		if (!isContainerTextFieldFocused(screen)) {
			return controller.charTyped(codePoint, modifiers);
		}
		return false;
	}

	/**
	 * Offers typed characters to JEI after a Minecraft text field has had the first chance
	 * to handle them. JEI inserts them only if its search field has keyboard focus.
	 */
	public void onKeyboardCharTypedPost(Screen screen, char codePoint, int modifiers) {
		if (!consumesCharacters() && isContainerTextFieldFocused(screen)) {
			controller.charTyped(codePoint, modifiers);
		}
	}

	public boolean onGuiMouseClicked(Screen screen, UserInput input) {
		keyInteraction = Optional.empty();
		return controller.mousePressed(screen, input);
	}

	public boolean onGuiMouseReleased(Screen screen, UserInput input) {
		return controller.mouseReleased(screen, input);
	}

	public boolean onGuiMouseScroll(double mouseX, double mouseY, double scrollDeltaY) {
		return onGuiMouseScroll(mouseX, mouseY, 0, scrollDeltaY);
	}

	public boolean onGuiMouseScroll(double mouseX, double mouseY, double scrollDeltaX, double scrollDeltaY) {
		return controller.mouseScrolled(mouseX, mouseY, scrollDeltaX, scrollDeltaY);
	}

	public boolean onGuiMouseDragged(Screen screen, double mouseX, double mouseY, int button, double dragX, double dragY) {
		InputConstants.Key input = InputConstants.Type.MOUSE.getOrCreate(button);
		return controller.mouseDragged(screen, mouseX, mouseY, input, dragX, dragY);
	}

	private boolean consumesCharacters() {
		return keyInteraction.map(IInputInteraction::consumesCharacters).orElse(false);
	}

	private boolean isContainerTextFieldFocused(Screen screen) {
		return GuiEventListenerUtil.getEditBoxes(screen)
			.anyMatch(textField -> textField.isActive() && textField.isFocused());
	}
}
