package mezz.jei.gui.input;

import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.api.runtime.IScreenHelper;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.core.util.ReflectionUtil;
import mezz.jei.gui.input.handlers.DragRouter;
import mezz.jei.gui.input.handlers.UserInputRouter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;

import java.util.List;

public class ClientInputHandler {
	private final List<ICharTypedHandler> charTypedHandlers;
	private final UserInputRouter inputRouter;
	private final DragRouter dragRouter;
	private final IInternalKeyMappings keybindings;
	private final IScreenHelper screenHelper;
	private final ReflectionUtil reflectionUtil = new ReflectionUtil();

	public ClientInputHandler(
		List<ICharTypedHandler> charTypedHandlers,
		UserInputRouter inputRouter,
		DragRouter dragRouter,
		IInternalKeyMappings keybindings,
		IScreenHelper screenHelper
	) {
		this.charTypedHandlers = charTypedHandlers;
		this.inputRouter = inputRouter;
		this.dragRouter = dragRouter;
		this.keybindings = keybindings;
		this.screenHelper = screenHelper;
	}

	public void onInitGui() {
		this.inputRouter.handleGuiChange();
		this.dragRouter.handleGuiChange();
	}

	/**
	 * When we have keyboard focus, use Pre
	 */
	public boolean onKeyboardKeyPressedPre(Screen screen, UserInput input) {
		if (!isContainerTextFieldFocused(screen)) {
			IGuiProperties guiProperties = screenHelper.getGuiProperties(screen).orElse(null);
			if (guiProperties != null) {
				return this.inputRouter.handleUserInput(screen, guiProperties, input, keybindings);
			}
		}
		return false;
	}

	/**
	 * Without keyboard focus, use Post
	 */
	public boolean onKeyboardKeyPressedPost(Screen screen, UserInput input) {
		if (isContainerTextFieldFocused(screen)) {
			IGuiProperties guiProperties = screenHelper.getGuiProperties(screen).orElse(null);
			if (guiProperties != null) {
				return this.inputRouter.handleUserInput(screen, guiProperties, input, keybindings);
			}
		}
		return false;
	}

	/**
	 * When we have keyboard focus, use Pre
	 */
	public boolean onKeyboardCharTypedPre(Screen screen, CharacterEvent event) {
		if (!isContainerTextFieldFocused(screen)) {
			return handleCharTyped(event);
		}
		return false;
	}

	/**
	 * Without keyboard focus, use Post
	 */
	public void onKeyboardCharTypedPost(Screen screen, CharacterEvent event) {
		if (isContainerTextFieldFocused(screen)) {
			handleCharTyped(event);
		}
	}

	public boolean onGuiMouseClicked(Screen screen, UserInput input) {
		IGuiProperties guiProperties = screenHelper.getGuiProperties(screen).orElse(null);
		if (guiProperties == null) {
			return false;
		}

		boolean handled = this.inputRouter.handleUserInput(screen, guiProperties, input, keybindings);

		if (Minecraft.getInstance().screen == screen && input.is(keybindings.getLeftClick())) {
			handled |= this.dragRouter.startDrag(screen, input);
		}
		return handled;
	}

	public boolean onGuiMouseReleased(Screen screen, UserInput input) {
		IGuiProperties guiProperties = screenHelper.getGuiProperties(screen).orElse(null);
		if (guiProperties == null) {
			return false;
		}

		boolean handled = this.inputRouter.handleUserInput(screen, guiProperties, input, keybindings);

		if (input.is(keybindings.getLeftClick())) {
			handled |= this.dragRouter.completeDrag(screen, input);
		}
		return handled;
	}

	public boolean onGuiMouseScroll(double mouseX, double mouseY, double scrollDeltaX, double scrollDeltaY) {
		return this.inputRouter.handleMouseScrolled(mouseX, mouseY, scrollDeltaX, scrollDeltaY);
	}

	private boolean handleCharTyped(CharacterEvent event) {
		return this.charTypedHandlers.stream()
			.filter(ICharTypedHandler::hasKeyboardFocus)
			.anyMatch(handler -> handler.onCharTyped(event));
	}

	private boolean isContainerTextFieldFocused(Screen screen) {
		return reflectionUtil.getFieldWithClass(screen, EditBox.class)
			.anyMatch(textField -> textField.isActive() && textField.isFocused());
	}
}
