package mezz.jei.fabric.startup;

import com.mojang.blaze3d.platform.Window;
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
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import org.jspecify.annotations.Nullable;

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
		ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) -> registerScreenEvents(screen));
		JeiCharTypedEvents.BEFORE_CHAR_TYPED.register(this::beforeCharTyped);
		ScreenEvents.AFTER_INIT.register(this::afterInit);
		JeiScreenEvents.DRAW_FOREGROUND.register(this::drawForeground);
		JeiScreenEvents.DRAW_BACKGROUND.register(this::drawBackground);
	}

	private void registerScreenEvents(Screen screen) {
		if (guiEventHandler == null) {
			return;
		}

		ScreenKeyboardEvents.allowKeyPress(screen).register(this::allowKeyPress);
		ScreenMouseEvents.allowMouseClick(screen).register(this::allowMouseClick);
		ScreenMouseEvents.allowMouseRelease(screen).register(this::allowMouseRelease);
		ScreenMouseEvents.allowMouseDrag(screen).register(this::allowMouseDrag);
		ScreenMouseEvents.allowMouseScroll(screen).register(this::allowMouseScroll);
		ScreenEvents.afterTick(screen).register(this::afterTick);
	}

	private boolean allowMouseClick(Screen screen, MouseButtonEvent event) {
		if (clientInputHandler == null) {
			return true;
		}
		return UserInput.fromVanilla(event, false, InputType.SIMULATE)
			.map(input -> !clientInputHandler.onGuiMouseClicked(screen, input))
			.orElse(true);
	}

	private boolean allowMouseRelease(Screen screen, MouseButtonEvent event) {
		if (clientInputHandler == null) {
			return true;
		}
		return UserInput.fromVanilla(event, false, InputType.EXECUTE)
			.map(input -> !clientInputHandler.onGuiMouseReleased(screen, input))
			.orElse(true);
	}

	private boolean allowKeyPress(Screen screen, KeyEvent keyEvent) {
		if (clientInputHandler == null) {
			getKeyboardHandlerExtension().jei$setConsumeNextCharTyped(false);
			return true;
		}
		boolean hadKeyboardFocus = hasJeiKeyboardFocus();
		UserInput userInput = UserInput.fromVanilla(keyEvent, InputType.IMMEDIATE);
		boolean consumed = clientInputHandler.onKeyboardKeyPressedPre(screen, userInput);
		boolean acquiredKeyboardFocus = !hadKeyboardFocus && hasJeiKeyboardFocus();
		boolean consumeNextCharTyped = consumed && acquiredKeyboardFocus;
		getKeyboardHandlerExtension().jei$setConsumeNextCharTyped(consumeNextCharTyped);
		return !consumed;
	}

	private boolean allowMouseScroll(Screen screen, double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (clientInputHandler == null) {
			return false;
		}
		return !clientInputHandler.onGuiMouseScroll(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	private boolean allowMouseDrag(Screen screen, MouseButtonEvent event, double horizontalAmount, double verticalAmount) {
		if (clientInputHandler == null) {
			return true;
		}
		return !clientInputHandler.onGuiMouseDragged(screen, event, horizontalAmount, verticalAmount);
	}

	private void afterTick(Screen screen) {
		if (guiEventHandler != null) {
			guiEventHandler.onClientTick();
		}
	}

	private boolean beforeCharTyped(long windowHandle, CharacterEvent event) {
		Minecraft minecraft = Minecraft.getInstance();
		Window window = minecraft.getWindow();
		if (window.handle() == windowHandle &&
			clientInputHandler != null &&
			minecraft.gui.screen() instanceof Screen screen &&
			minecraft.gui.overlay() == null
		) {
			return clientInputHandler.onKeyboardCharTypedPre(screen, event);
		}
		return false;
	}

	private void afterInit(Minecraft client, Screen screen, int scaledWidth, int scaledHeight) {
		if (guiEventHandler != null) {
			guiEventHandler.onGuiInit(screen);
			guiEventHandler.onGuiOpen(screen);
		}
	}

	private void drawForeground(Screen screen, GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		if (guiEventHandler != null) {
			guiEventHandler.drawForScreenForeground(screen, guiGraphics, mouseX, mouseY);
		}
	}

	private void drawBackground(Screen screen, GuiGraphicsExtractor guiGraphics) {
		if (guiEventHandler != null) {
			guiEventHandler.drawForScreenBackground(screen, guiGraphics);
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
