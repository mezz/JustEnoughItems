package mezz.jei.input;

import mezz.jei.config.KeyBindings;
import mezz.jei.events.EventBusHelper;
import mezz.jei.input.mouse.ICharTypedHandler;
import mezz.jei.input.mouse.handlers.CombinedInputHandler;
import mezz.jei.util.ReflectionUtil;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.client.event.ScreenEvent;

import java.util.List;

public class InputEventHandler {
	private final List<ICharTypedHandler> charTypedHandlers;
	private final CombinedInputHandler inputHandler;

	public InputEventHandler(List<ICharTypedHandler> charTypedHandlers, CombinedInputHandler inputHandler) {
		this.charTypedHandlers = charTypedHandlers;
		this.inputHandler = inputHandler;
	}

	public void registerToEventBus() {
		EventBusHelper.registerWeakListener(this, ScreenEvent.InitScreenEvent.class, InputEventHandler::onInitGuiEvent);

		EventBusHelper.registerWeakListener(this, ScreenEvent.KeyboardKeyPressedEvent.Pre.class, InputEventHandler::onKeyboardKeyPressedEvent);
		EventBusHelper.registerWeakListener(this, ScreenEvent.KeyboardKeyPressedEvent.Post.class, InputEventHandler::onKeyboardKeyPressedEvent);

		EventBusHelper.registerWeakListener(this, ScreenEvent.KeyboardCharTypedEvent.Pre.class, InputEventHandler::onKeyboardCharTypedEvent);
		EventBusHelper.registerWeakListener(this, ScreenEvent.KeyboardCharTypedEvent.Post.class, InputEventHandler::onKeyboardCharTypedEvent);

		EventBusHelper.registerWeakListener(this, ScreenEvent.MouseClickedEvent.Pre.class, InputEventHandler::onGuiMouseClickedEvent);
		EventBusHelper.registerWeakListener(this, ScreenEvent.MouseReleasedEvent.Pre.class, InputEventHandler::onGuiMouseReleasedEvent);

		EventBusHelper.registerWeakListener(this, ScreenEvent.MouseScrollEvent.Pre.class, InputEventHandler::onGuiMouseScrollEvent);
	}

	public void onInitGuiEvent(ScreenEvent.InitScreenEvent event) {
		this.inputHandler.handleGuiChange();
	}

	/**
	 * When we have keyboard focus, use Pre
	 */
	public void onKeyboardKeyPressedEvent(ScreenEvent.KeyboardKeyPressedEvent.Pre event) {
		Screen screen = event.getScreen();
		if (!isContainerTextFieldFocused(screen)) {
			UserInput input = UserInput.fromEvent(event);
			this.inputHandler.handleUserInput(screen, input)
				.ifPresent(handler -> event.setCanceled(true));
		}
	}

	/**
	 * Without keyboard focus, use Post
	 */
	public void onKeyboardKeyPressedEvent(ScreenEvent.KeyboardKeyPressedEvent.Post event) {
		Screen screen = event.getScreen();
		if (isContainerTextFieldFocused(screen)) {
			UserInput input = UserInput.fromEvent(event);
			this.inputHandler.handleUserInput(screen, input)
				.ifPresent(handler -> event.setCanceled(true));
		}
	}

	/**
	 * When we have keyboard focus, use Pre
	 */
	public void onKeyboardCharTypedEvent(ScreenEvent.KeyboardCharTypedEvent.Pre event) {
		if (!isContainerTextFieldFocused(event.getScreen())) {
			if (handleCharTyped(event.getCodePoint(), event.getModifiers())) {
				event.setCanceled(true);
			}
		}
	}

	/**
	 * Without keyboard focus, use Post
	 */
	public void onKeyboardCharTypedEvent(ScreenEvent.KeyboardCharTypedEvent.Post event) {
		if (isContainerTextFieldFocused(event.getScreen())) {
			if (handleCharTyped(event.getCodePoint(), event.getModifiers())) {
				event.setCanceled(true);
			}
		}
	}

	public void onGuiMouseClickedEvent(ScreenEvent.MouseClickedEvent.Pre event) {
		UserInput.fromEvent(event)
			.ifPresent(input -> {
				Screen screen = event.getScreen();
				this.inputHandler.handleUserInput(screen, input)
					.ifPresent(handled -> event.setCanceled(true));

				if (input.is(KeyBindings.leftClick)) {
					this.inputHandler.handleDragStart(screen, input)
						.ifPresent(handled -> event.setCanceled(true));
				}
			});
	}

	public void onGuiMouseReleasedEvent(ScreenEvent.MouseReleasedEvent.Pre event) {
		UserInput.fromEvent(event)
			.ifPresent(input -> {
				Screen screen = event.getScreen();

				this.inputHandler.handleUserInput(screen, input)
					.ifPresent(handled -> event.setCanceled(true));

				if (input.is(KeyBindings.leftClick)) {
					this.inputHandler.handleDragComplete(screen, input)
						.ifPresent(handled -> event.setCanceled(true));
				}
			});
	}

	public void onGuiMouseScrollEvent(ScreenEvent.MouseScrollEvent.Pre event) {
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
