package mezz.jei.gui.input;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.api.runtime.IScreenHelper;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.util.ReflectionUtil;
import mezz.jei.gui.input.handlers.ChatLinkInputHandler;
import mezz.jei.gui.input.handlers.DragRouter;
import mezz.jei.gui.input.handlers.UserInputRouter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.MouseButtonEvent;

import java.util.List;

public class ClientInputHandler {
	private final List<ICharTypedHandler> charTypedHandlers;
	private final ChatLinkInputHandler chatLinkInputHandler;
	private final UserInputRouter inputRouter;
	private final DragRouter dragRouter;
	private final IInternalKeyMappings keybindings;
	private final IScreenHelper screenHelper;
	private final ReflectionUtil reflectionUtil = new ReflectionUtil();

	public ClientInputHandler(
		List<ICharTypedHandler> charTypedHandlers,
		ChatLinkInputHandler chatLinkInputHandler,
		UserInputRouter inputRouter,
		DragRouter dragRouter,
		IInternalKeyMappings keybindings,
		IScreenHelper screenHelper
	) {
		this.charTypedHandlers = charTypedHandlers;
		this.chatLinkInputHandler = chatLinkInputHandler;
		this.inputRouter = inputRouter;
		this.dragRouter = dragRouter;
		this.keybindings = keybindings;
		this.screenHelper = screenHelper;
	}

	public void onInitGui() {
		this.chatLinkInputHandler.handleGuiChange();
		this.inputRouter.handleGuiChange();
		this.dragRouter.handleGuiChange();
	}

	/**
	 * When we have keyboard focus, use Pre
	 */
	public boolean onKeyboardKeyPressedPre(Screen screen, UserInput input) {
		if (this.chatLinkInputHandler.handleUserInput(screen, input, keybindings)) {
			return true;
		}

		// Focus-search explicitly transfers focus, including from the creative inventory's always-focused search box.
		if (input.is(keybindings.getFocusSearch()) || !isContainerTextFieldFocused(screen)) {
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
		if (this.chatLinkInputHandler.handleUserInput(screen, input, keybindings)) {
			return true;
		}

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
		if (this.chatLinkInputHandler.handleUserInput(screen, input, keybindings)) {
			return true;
		}

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

	public boolean onGuiMouseDragged(Screen screen, MouseButtonEvent event, double dragX, double dragY) {
		InputConstants.Key input = InputConstants.Type.MOUSE.getOrCreate(event.button());
		return this.inputRouter.handleMouseDragged(event.x(), event.y(), input, dragX, dragY);
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
