package mezz.jei.gui.input;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.input.UserInput;
import mezz.jei.common.util.ReflectionUtil;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.MouseButtonEvent;

/** Connects Minecraft's input callbacks to JEI's input controller. */
public class ClientInputHandler {
	private final GuiInputController controller;
	private final IInternalKeyMappings keybindings;
	private final ReflectionUtil reflectionUtil = new ReflectionUtil();

	public ClientInputHandler(GuiInputController controller, IInternalKeyMappings keybindings) {
		this.controller = controller;
		this.keybindings = keybindings;
	}

	public void onGuiChanged() {
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
		// Focus Search transfers keyboard focus even when the creative inventory's search field is selected.
		if (screen instanceof ChatScreen || input.is(keybindings.getFocusSearch()) || !isContainerTextFieldFocused(screen)) {
			return controller.keyPressed(screen, input);
		}
		return false;
	}

	/**
	 * Offers a key press to JEI after the Minecraft screen has declined it, when a Minecraft
	 * text field has keyboard focus. This lets that text field handle the key first.
	 */
	public boolean onKeyboardKeyPressedPost(Screen screen, UserInput input) {
		if (!(screen instanceof ChatScreen) && isContainerTextFieldFocused(screen)) {
			return controller.keyPressed(screen, input);
		}
		return false;
	}

	/**
	 * Offers typed characters to JEI before the Minecraft screen handles them, when no
	 * Minecraft text field has keyboard focus. JEI inserts them only if its search field
	 * has keyboard focus.
	 */
	public boolean onKeyboardCharTypedPre(Screen screen, CharacterEvent event) {
		if (!isContainerTextFieldFocused(screen)) {
			return controller.charTyped(event);
		}
		return false;
	}

	/**
	 * Offers typed characters to JEI after a Minecraft text field has had the first chance
	 * to handle them. JEI inserts them only if its search field has keyboard focus.
	 */
	public void onKeyboardCharTypedPost(Screen screen, CharacterEvent event) {
		if (isContainerTextFieldFocused(screen)) {
			controller.charTyped(event);
		}
	}

	public boolean onGuiMouseClicked(Screen screen, UserInput input) {
		return controller.mousePressed(screen, input);
	}

	public boolean onGuiMouseReleased(Screen screen, UserInput input) {
		return controller.mouseReleased(screen, input);
	}

	public boolean onGuiMouseScroll(double mouseX, double mouseY, double scrollDeltaX, double scrollDeltaY) {
		return controller.mouseScrolled(mouseX, mouseY, scrollDeltaX, scrollDeltaY);
	}

	public boolean onGuiMouseDragged(Screen screen, MouseButtonEvent event, double dragX, double dragY) {
		InputConstants.Key input = InputConstants.Type.MOUSE.getOrCreate(event.button());
		return controller.mouseDragged(screen, event.x(), event.y(), input, dragX, dragY);
	}

	private boolean isContainerTextFieldFocused(Screen screen) {
		return reflectionUtil.getFieldWithClass(screen, EditBox.class)
			.anyMatch(textField -> textField.isActive() && textField.isFocused());
	}
}
