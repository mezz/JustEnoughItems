package mezz.jei.fabric.startup;

import mezz.jei.common.Internal;
import mezz.jei.fabric.events.JeiCharTypedEvents;
import mezz.jei.fabric.events.JeiScreenEvents;
import mezz.jei.fabric.input.KeyboardHandlerExtension;
import mezz.jei.gui.events.GuiEventHandler;
import mezz.jei.gui.input.ClientInputHandler;
import mezz.jei.gui.input.InputType;
import mezz.jei.gui.input.UserInput;
import mezz.jei.gui.startup.JeiEventHandlers;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
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
		ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) ->
			registerScreenEvents(screen)
		);
		JeiCharTypedEvents.BEFORE_CHAR_TYPED.register(this::beforeCharTyped);
		ScreenEvents.AFTER_INIT.register(this::afterInit);
		JeiScreenEvents.DRAW_BACKGROUND.register(this::drawBackground);
		JeiScreenEvents.DRAW_FOREGROUND.register(this::drawForeground);
		JeiScreenEvents.ALLOW_MOUSE_DRAG.register(this::allowMouseDrag);
	}

	private void registerScreenEvents(Screen screen) {
		if (guiEventHandler == null) {
			return;
		}

		ScreenKeyboardEvents.allowKeyPress(screen).register(this::allowKeyPress);
		ScreenMouseEvents.allowMouseClick(screen).register(this::allowMouseClick);
		ScreenMouseEvents.allowMouseRelease(screen).register(this::allowMouseRelease);
		ScreenMouseEvents.allowMouseScroll(screen).register(this::allowMouseScroll);
		ScreenEvents.afterTick(screen).register(this::afterTick);
	}

	private boolean allowMouseClick(Screen screen, double mouseX, double mouseY, int button) {
		if (clientInputHandler == null) {
			return true;
		}
		return UserInput.fromVanilla(mouseX, mouseY, button, InputType.SIMULATE)
			.map(input -> !clientInputHandler.onGuiMouseClicked(screen, input))
			.orElse(true);
	}

	private boolean allowMouseRelease(Screen screen, double mouseX, double mouseY, int button) {
		if (clientInputHandler == null) {
			return true;
		}
		return UserInput.fromVanilla(mouseX, mouseY, button, InputType.EXECUTE)
			.map(input -> !clientInputHandler.onGuiMouseReleased(screen, input))
			.orElse(true);
	}

	private boolean allowKeyPress(Screen screen, int key, int scancode, int modifiers) {
		if (clientInputHandler == null) {
			getKeyboardHandlerExtension().jei$setConsumeNextCharTyped(false);
			return true;
		}
		boolean hadKeyboardFocus = hasJeiKeyboardFocus();
		UserInput userInput = UserInput.fromVanilla(key, scancode, modifiers, InputType.IMMEDIATE);
		boolean consumed = clientInputHandler.onKeyboardKeyPressedPre(screen, userInput);
		boolean acquiredKeyboardFocus = !hadKeyboardFocus && hasJeiKeyboardFocus();
		getKeyboardHandlerExtension().jei$setConsumeNextCharTyped(consumed && acquiredKeyboardFocus);
		return !consumed;
	}

	private boolean allowMouseScroll(Screen screen, double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (clientInputHandler == null) {
			return false;
		}
		return !clientInputHandler.onGuiMouseScroll(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	private boolean allowMouseDrag(Screen screen, double mouseX, double mouseY, int button, double dragX, double dragY) {
		if (clientInputHandler == null) {
			return true;
		}
		return !clientInputHandler.onGuiMouseDragged(screen, mouseX, mouseY, button, dragX, dragY);
	}

	private void afterTick(Screen screen) {
		if (guiEventHandler != null) {
			guiEventHandler.onClientTick();
		}
	}

	private boolean beforeCharTyped(GuiEventListener guiEventListener, char codepoint, int modifiers) {
		if (clientInputHandler != null && guiEventListener instanceof Screen screen) {
			return clientInputHandler.onKeyboardCharTypedPre(screen, codepoint, modifiers);
		}
		return false;
	}

	private void afterInit(Minecraft client, Screen screen, int scaledWidth, int scaledHeight) {
		if (guiEventHandler != null) {
			guiEventHandler.onGuiInit(screen);
			guiEventHandler.onGuiOpen(screen);
		}
	}

	private void drawBackground(Screen screen, GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		if (guiEventHandler != null) {
			guiEventHandler.drawForScreenBackground(screen, guiGraphics);
		}
	}

	private void drawForeground(Screen screen, GuiGraphics guiGraphics, int mouseX, int mouseY) {
		if (guiEventHandler != null) {
			guiEventHandler.drawForScreenForeground(screen, guiGraphics, mouseX, mouseY);
		}
	}

	public void clear() {
		this.clientInputHandler = null;
		this.guiEventHandler = null;
		getKeyboardHandlerExtension().jei$setConsumeNextCharTyped(false);
	}

	private static KeyboardHandlerExtension getKeyboardHandlerExtension() {
		return (KeyboardHandlerExtension) Minecraft.getInstance().keyboardHandler;
	}

	private static boolean hasJeiKeyboardFocus() {
		return Internal.getOptionalJeiRuntime()
			.map(runtime -> runtime.getIngredientListOverlay().hasKeyboardFocus())
			.orElse(false);
	}
}
