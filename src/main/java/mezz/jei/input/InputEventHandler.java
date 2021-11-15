package mezz.jei.input;

import mezz.jei.events.EventBusHelper;
import mezz.jei.input.mouse.ICharTypedHandler;
import mezz.jei.input.mouse.IUserInputHandler;
import mezz.jei.input.mouse.handlers.CombinedUserInputHandler;
import mezz.jei.util.ReflectionUtil;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.client.event.GuiScreenEvent;

import java.util.List;

public class InputEventHandler {
	private final List<ICharTypedHandler> charTypedHandlers;
	private final CombinedUserInputHandler inputHandler;

	public InputEventHandler(List<ICharTypedHandler> charTypedHandlers, CombinedUserInputHandler inputHandler) {
		this.charTypedHandlers = charTypedHandlers;
		this.inputHandler = inputHandler;
	}

	public void registerToEventBus() {
		EventBusHelper.registerWeakListener(this, GuiScreenEvent.InitGuiEvent.class, InputEventHandler::onInitGuiEvent);

		EventBusHelper.registerWeakListener(this, GuiScreenEvent.KeyboardKeyPressedEvent.Pre.class, InputEventHandler::onKeyboardKeyPressedEvent);
		EventBusHelper.registerWeakListener(this, GuiScreenEvent.KeyboardKeyPressedEvent.Post.class, InputEventHandler::onKeyboardKeyPressedEvent);

		EventBusHelper.registerWeakListener(this, GuiScreenEvent.KeyboardCharTypedEvent.Pre.class, InputEventHandler::onKeyboardCharTypedEvent);
		EventBusHelper.registerWeakListener(this, GuiScreenEvent.KeyboardCharTypedEvent.Post.class, InputEventHandler::onKeyboardCharTypedEvent);

		EventBusHelper.registerWeakListener(this, GuiScreenEvent.MouseClickedEvent.Pre.class, InputEventHandler::onGuiMouseClickedEvent);
		EventBusHelper.registerWeakListener(this, GuiScreenEvent.MouseReleasedEvent.Pre.class, InputEventHandler::onGuiMouseReleasedEvent);

		EventBusHelper.registerWeakListener(this, GuiScreenEvent.MouseScrollEvent.Pre.class, InputEventHandler::onGuiMouseScrollEvent);
	}

	public void onInitGuiEvent(GuiScreenEvent.InitGuiEvent event) {
		this.inputHandler.handleGuiChange();
	}

	/**
	 * When we have keyboard focus, use Pre
	 */
	public void onKeyboardKeyPressedEvent(GuiScreenEvent.KeyboardKeyPressedEvent.Pre event) {
		Screen screen = event.getGui();
		if (!isContainerTextFieldFocused(screen)) {
			UserInput input = UserInput.fromEvent(event);
			IUserInputHandler handler = this.inputHandler.handleUserInput(screen, input);
			if (handler != null) {
				event.setCanceled(true);
			}
		}
	}

	/**
	 * Without keyboard focus, use Post
	 */
	public void onKeyboardKeyPressedEvent(GuiScreenEvent.KeyboardKeyPressedEvent.Post event) {
		Screen screen = event.getGui();
		if (isContainerTextFieldFocused(screen)) {
			UserInput input = UserInput.fromEvent(event);
			IUserInputHandler handler = this.inputHandler.handleUserInput(screen, input);
			if (handler != null) {
				event.setCanceled(true);
			}
		}
	}

	/**
	 * When we have keyboard focus, use Pre
	 */
	public void onKeyboardCharTypedEvent(GuiScreenEvent.KeyboardCharTypedEvent.Pre event) {
		if (!isContainerTextFieldFocused(event.getGui())) {
			if (handleCharTyped(event.getCodePoint(), event.getModifiers())) {
				event.setCanceled(true);
			}
		}
	}

	/**
	 * Without keyboard focus, use Post
	 */
	public void onKeyboardCharTypedEvent(GuiScreenEvent.KeyboardCharTypedEvent.Post event) {
		if (isContainerTextFieldFocused(event.getGui())) {
			if (handleCharTyped(event.getCodePoint(), event.getModifiers())) {
				event.setCanceled(true);
			}
		}
	}

	public void onGuiMouseClickedEvent(GuiScreenEvent.MouseClickedEvent.Pre event) {
		UserInput input = UserInput.fromEvent(event);
		if (input != null) {
			Screen screen = event.getGui();
			IUserInputHandler handler = this.inputHandler.handleUserInput(screen, input);
			IUserInputHandler dragHandler = null;
			if (input.isLeftClick()) {
				dragHandler = this.inputHandler.handleDragStart(screen, input);
			}
			if (handler != null || dragHandler != null) {
				event.setCanceled(true);
			}
		}
	}

	public void onGuiMouseReleasedEvent(GuiScreenEvent.MouseReleasedEvent.Pre event) {
		UserInput input = UserInput.fromEvent(event);
		if (input != null) {
			Screen screen = event.getGui();
			IUserInputHandler handled = this.inputHandler.handleUserInput(screen, input);
			IUserInputHandler dragHandled = null;
			if (input.isLeftClick()) {
				dragHandled = this.inputHandler.handleDragComplete(screen, input);
			}
			if (handled != null || dragHandled != null) {
				event.setCanceled(true);
			}
		}
	}

	public void onGuiMouseScrollEvent(GuiScreenEvent.MouseScrollEvent.Pre event) {
		if (this.inputHandler.handleMouseScrolled(event.getMouseX(), event.getMouseY(), event.getScrollDelta())) {
			event.setCanceled(true);
		}
	}

	private boolean handleCharTyped(char codePoint, int modifiers) {
		return this.charTypedHandlers.stream()
			.filter(ICharTypedHandler::hasKeyboardFocus)
			.anyMatch(handler -> handler.onCharTyped(codePoint, modifiers));
	}

	private static boolean isContainerTextFieldFocused(Screen screen) {
		EditBox textField = ReflectionUtil.getFieldWithClass(screen, EditBox.class);
		return textField != null && textField.isActive() && textField.isFocused();
	}

}
