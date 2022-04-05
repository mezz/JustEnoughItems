package mezz.jei.forge.input;

import mezz.jei.common.input.ICharTypedHandler;
import mezz.jei.common.input.IKeyBindings;
import mezz.jei.common.input.UserInput;
import mezz.jei.core.util.ReflectionUtil;
import mezz.jei.forge.events.RuntimeEventSubscriptions;
import mezz.jei.input.mouse.handlers.CombinedInputHandler;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.client.event.ScreenEvent;

import java.util.List;

public class InputEventHandler {
	private final List<ICharTypedHandler> charTypedHandlers;
	private final CombinedInputHandler inputHandler;
	private final IKeyBindings keybindings;
	private final ReflectionUtil reflectionUtil = new ReflectionUtil();

	public InputEventHandler(List<ICharTypedHandler> charTypedHandlers, CombinedInputHandler inputHandler, IKeyBindings keybindings) {
		this.charTypedHandlers = charTypedHandlers;
		this.inputHandler = inputHandler;
		this.keybindings = keybindings;
	}

	public void register(RuntimeEventSubscriptions subscriptions) {
		subscriptions.register(ScreenEvent.InitScreenEvent.class, this::onInitGuiEvent);

		subscriptions.register(ScreenEvent.KeyboardKeyPressedEvent.Pre.class, this::onKeyboardKeyPressedEvent);
		subscriptions.register(ScreenEvent.KeyboardKeyPressedEvent.Post.class, this::onKeyboardKeyPressedEvent);

		subscriptions.register(ScreenEvent.KeyboardCharTypedEvent.Pre.class, this::onKeyboardCharTypedEvent);
		subscriptions.register(ScreenEvent.KeyboardCharTypedEvent.Post.class, this::onKeyboardCharTypedEvent);

		subscriptions.register(ScreenEvent.MouseClickedEvent.Pre.class, this::onGuiMouseClickedEvent);
		subscriptions.register(ScreenEvent.MouseReleasedEvent.Pre.class, this::onGuiMouseReleasedEvent);

		subscriptions.register(ScreenEvent.MouseScrollEvent.Pre.class, this::onGuiMouseScrollEvent);
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
			UserInput input = ForgeUserInput.fromEvent(event);
			this.inputHandler.handleUserInput(screen, input, keybindings)
				.ifPresent(handler -> event.setCanceled(true));
		}
	}

	/**
	 * Without keyboard focus, use Post
	 */
	public void onKeyboardKeyPressedEvent(ScreenEvent.KeyboardKeyPressedEvent.Post event) {
		Screen screen = event.getScreen();
		if (isContainerTextFieldFocused(screen)) {
			UserInput input = ForgeUserInput.fromEvent(event);
			this.inputHandler.handleUserInput(screen, input, keybindings)
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
		ForgeUserInput.fromEvent(event)
			.ifPresent(input -> {
				Screen screen = event.getScreen();
				this.inputHandler.handleUserInput(screen, input, keybindings)
					.ifPresent(handled -> event.setCanceled(true));

				if (input.is(keybindings.getLeftClick())) {
					this.inputHandler.handleDragStart(screen, input)
						.ifPresent(handled -> event.setCanceled(true));
				}
			});
	}

	public void onGuiMouseReleasedEvent(ScreenEvent.MouseReleasedEvent.Pre event) {
		ForgeUserInput.fromEvent(event)
			.ifPresent(input -> {
				Screen screen = event.getScreen();

				this.inputHandler.handleUserInput(screen, input, keybindings)
					.ifPresent(handled -> event.setCanceled(true));

				if (input.is(keybindings.getLeftClick())) {
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

	private boolean isContainerTextFieldFocused(Screen screen) {
		return reflectionUtil.getFieldWithClass(screen, EditBox.class)
			.anyMatch(textField -> textField.isActive() && textField.isFocused());
	}
}
