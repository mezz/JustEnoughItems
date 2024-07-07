package mezz.jei.fabric.startup;

import mezz.jei.fabric.events.JeiCharTypedEvents;
import mezz.jei.fabric.events.JeiScreenEvents;
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
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
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
		JeiScreenEvents.DRAW_FOREGROUND.register(this::drawForeground);
	}

	private void registerScreenEvents(Screen screen) {
		if (guiEventHandler == null) {
			return;
		}

		ScreenKeyboardEvents.allowKeyPress(screen).register(this::allowKeyPress);
		ScreenMouseEvents.allowMouseClick(screen).register(this::allowMouseClick);
		ScreenMouseEvents.beforeMouseRelease(screen).register(this::beforeMouseRelease);
		ScreenMouseEvents.allowMouseScroll(screen).register(this::allowMouseScroll);
		ScreenEvents.afterRender(screen).register(this::afterRender);
	}

	private boolean allowMouseClick(Screen screen, double mouseX, double mouseY, int button) {
		if (clientInputHandler == null) {
			return true;
		}
		return UserInput.fromVanilla(mouseX, mouseY, button, InputType.SIMULATE)
			.map(input -> !clientInputHandler.onGuiMouseClicked(screen, input))
			.orElse(true);
	}

	@SuppressWarnings("UnusedReturnValue")
	private boolean beforeMouseRelease(Screen screen, double mouseX, double mouseY, int button) {
		if (clientInputHandler == null) {
			return true;
		}
		return UserInput.fromVanilla(mouseX, mouseY, button, InputType.EXECUTE)
			.map(input -> !clientInputHandler.onGuiMouseReleased(screen, input))
			.orElse(true);
	}

	private boolean allowKeyPress(Screen screen, int key, int scancode, int modifiers) {
		if (clientInputHandler == null) {
			return true;
		}
		UserInput userInput = UserInput.fromVanilla(key, scancode, modifiers, InputType.IMMEDIATE);
		return !clientInputHandler.onKeyboardKeyPressedPre(screen, userInput);
	}

	private boolean allowMouseScroll(Screen screen, double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (clientInputHandler == null) {
			return false;
		}
		return !clientInputHandler.onGuiMouseScroll(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	private void afterRender(Screen screen, GuiGraphics guiGraphics, int mouseX, int mouseY, float tickDelta) {
		if (guiEventHandler != null) {
			guiEventHandler.onDrawScreenPost(screen, guiGraphics, mouseX, mouseY);
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

	private void drawForeground(AbstractContainerScreen<?> screen, GuiGraphics guiGraphics, int mouseX, int mouseY) {
		if (guiEventHandler != null) {
			guiEventHandler.onDrawForeground(screen, guiGraphics, mouseX, mouseY);
		}
	}

	public void clear() {
		this.clientInputHandler = null;
		this.guiEventHandler = null;
	}
}
