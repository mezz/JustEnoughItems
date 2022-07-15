package mezz.jei.common.input;

import mezz.jei.common.input.handlers.CombinedInputHandler;
import mezz.jei.core.util.ReflectionUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;

import java.util.List;

public class ClientInputHandler {
	private final List<ICharTypedHandler> charTypedHandlers;
	private final CombinedInputHandler inputHandler;
	private final IKeyBindings keybindings;
	private final ReflectionUtil reflectionUtil = new ReflectionUtil();

	public ClientInputHandler(List<ICharTypedHandler> charTypedHandlers, CombinedInputHandler inputHandler, IKeyBindings keybindings) {
		this.charTypedHandlers = charTypedHandlers;
		this.inputHandler = inputHandler;
		this.keybindings = keybindings;
	}

	public void onInitGui() {
		this.inputHandler.handleGuiChange();
	}

	/**
	 * When we have keyboard focus, use Pre
	 */
	public boolean onKeyboardKeyPressedPre(Screen screen, UserInput input) {
		if (!isContainerTextFieldFocused(screen)) {
			return this.inputHandler.handleUserInput(screen, input, keybindings)
				.isPresent();
		}
		return false;
	}

	/**
	 * Without keyboard focus, use Post
	 */
	public boolean onKeyboardKeyPressedPost(Screen screen, UserInput input) {
		if (isContainerTextFieldFocused(screen)) {
			return this.inputHandler.handleUserInput(screen, input, keybindings)
				.isPresent();
		}
		return false;
	}

	/**
	 * When we have keyboard focus, use Pre
	 */
	public boolean onKeyboardCharTypedPre(Screen screen, char codePoint, int modifiers) {
		if (!isContainerTextFieldFocused(screen)) {
			return handleCharTyped(codePoint, modifiers);
		}
		return false;
	}

	/**
	 * Without keyboard focus, use Post
	 */
	public boolean onKeyboardCharTypedPost(Screen screen, char codePoint, int modifiers) {
		if (isContainerTextFieldFocused(screen)) {
			return handleCharTyped(codePoint, modifiers);
		}
		return false;
	}

	public boolean onGuiMouseClicked(Screen screen, UserInput input) {
		boolean handled = this.inputHandler.handleUserInput(screen, input, keybindings)
			.isPresent();

		if (Minecraft.getInstance().screen == screen && input.is(keybindings.getLeftClick())) {
			handled |= this.inputHandler.handleDragStart(screen, input)
				.isPresent();
		}
		return handled;
	}

	public boolean onGuiMouseReleased(Screen screen, UserInput input) {
		boolean handled = this.inputHandler.handleUserInput(screen, input, keybindings)
			.isPresent();

		if (input.is(keybindings.getLeftClick())) {
			handled |= this.inputHandler.handleDragComplete(screen, input)
				.isPresent();
		}
		return handled;
	}

	public boolean onGuiMouseScroll(double mouseX, double mouseY, double scrollDelta) {
		return this.inputHandler.handleMouseScrolled(mouseX, mouseY, scrollDelta);
	}

	private boolean handleCharTyped(char codePoint, int modifiers) {
		return this.charTypedHandlers.stream()
			.filter(ICharTypedHandler::hasKeyboardFocus)
			.anyMatch(handler -> handler.onCharTyped(codePoint, modifiers));
	}

	private boolean isContainerTextFieldFocused(Screen screen) {
		return reflectionUtil.getFieldWithClass(screen, EditBox.class)
			.anyMatch(textField -> textField.isActive() && textField.isFocused());
	}
}
