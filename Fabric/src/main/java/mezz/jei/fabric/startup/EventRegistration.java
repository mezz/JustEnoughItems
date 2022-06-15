package mezz.jei.fabric.startup;

import mezz.jei.common.gui.GuiEventHandler;
import mezz.jei.common.input.ClientInputHandler;
import mezz.jei.common.input.InputType;
import mezz.jei.common.input.UserInput;
import mezz.jei.common.startup.JeiEventHandlers;
import mezz.jei.fabric.events.JeiCharTypedEvents;
import mezz.jei.fabric.events.JeiScreenEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;

public class EventRegistration {
	@Nullable
	private ClientInputHandler clientInputHandler;
	@Nullable
	private GuiEventHandler guiEventHandler;
	private boolean registered;

	public void setEventHandlers(JeiEventHandlers eventHandlers) {
		clientInputHandler = eventHandlers.clientInputHandler();
		guiEventHandler = eventHandlers.guiEventHandler();
		if (!registered) {
			registerEvents();
			registered = true;
		}
	}

	private void registerEvents() {
		ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			if (guiEventHandler != null) {
				guiEventHandler.onGuiInit(screen);

				ScreenKeyboardEvents.allowKeyPress(screen).register((screen1, key, scancode, modifiers) -> {
					if (clientInputHandler != null) {
						UserInput userInput = UserInput.fromVanilla(key, scancode, modifiers, InputType.IMMEDIATE);
						return !clientInputHandler.onKeyboardKeyPressedPre(screen1, userInput);
					}
					return true;
				});

				ScreenMouseEvents.allowMouseClick(screen).register((screen1, mouseX, mouseY, button) -> {
					if (clientInputHandler == null) {
						return true;
					}
					return UserInput.fromVanilla(mouseX, mouseY, button, InputType.IMMEDIATE)
						.map(input -> !clientInputHandler.onGuiMouseClicked(screen1, input))
						.orElse(true);
				});

				ScreenMouseEvents.allowMouseScroll(screen).register((screen1, mouseX, mouseY, horizontalAmount, verticalAmount) -> {
					if (clientInputHandler == null) {
						return false;
					}
					return !clientInputHandler.onGuiMouseScroll(mouseX, mouseY, verticalAmount);
				});

				ScreenEvents.afterRender(screen).register((screen1, poseStack, mouseX, mouseY, tickDelta) -> {
					if (guiEventHandler != null) {
						guiEventHandler.onDrawScreenPost(screen1, poseStack, mouseX, mouseY);
					}
				});
			}
		});

		JeiCharTypedEvents.BEFORE_CHAR_TYPED.register((guiEventListener, codepoint, modifiers) -> {
			if (clientInputHandler != null && guiEventListener instanceof Screen screen) {
				return clientInputHandler.onKeyboardCharTypedPre(screen, codepoint, modifiers);
			}
			return false;
		});

		ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			if (guiEventHandler != null) {
				guiEventHandler.onGuiOpen(screen);
			}
		});

		JeiScreenEvents.AFTER_RENDER_BACKGROUND.register((screen, poseStack) -> {
			if (guiEventHandler != null) {
				guiEventHandler.onDrawBackgroundPost(screen, poseStack);
			}
		});

		JeiScreenEvents.DRAW_FOREGROUND.register(((screen, poseStack, mouseX, mouseY) -> {
			if (guiEventHandler != null) {
				guiEventHandler.onDrawForeground(screen, poseStack, mouseX, mouseY);
			}
		}));

		ClientTickEvents.START_CLIENT_TICK.register(client -> {
			if (guiEventHandler != null) {
				guiEventHandler.onClientTick();
			}
		});
	}

	public void clear() {
		this.clientInputHandler = null;
		this.guiEventHandler = null;
	}
}
