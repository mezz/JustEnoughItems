package mezz.jei.forge.startup;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.common.gui.GuiEventHandler;
import mezz.jei.common.input.ClientInputHandler;
import mezz.jei.common.input.UserInput;
import mezz.jei.common.startup.JeiEventHandlers;
import mezz.jei.forge.events.RuntimeEventSubscriptions;
import mezz.jei.forge.input.ForgeUserInput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraftforge.client.event.ContainerScreenEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.event.ScreenOpenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.Event;

public class EventRegistration {
	public static void registerEvents(RuntimeEventSubscriptions subscriptions, JeiEventHandlers eventHandlers) {
		ClientInputHandler clientInputHandler = eventHandlers.clientInputHandler();
		registerClientInputHandler(subscriptions, clientInputHandler);

		GuiEventHandler guiEventHandler = eventHandlers.guiEventHandler();
		registerGuiHandler(subscriptions, guiEventHandler);
	}

	private static void registerClientInputHandler(RuntimeEventSubscriptions subscriptions, ClientInputHandler handler) {
		subscriptions.register(ScreenEvent.InitScreenEvent.class, event -> handler.onInitGui());

		subscriptions.register(ScreenEvent.KeyboardKeyPressedEvent.Pre.class, event -> {
			Screen screen = event.getScreen();
			UserInput input = ForgeUserInput.fromEvent(event);
			if (handler.onKeyboardKeyPressedPre(screen, input)) {
				event.setCanceled(true);
			}
		});
		subscriptions.register(ScreenEvent.KeyboardKeyPressedEvent.Post.class, event -> {
			Screen screen = event.getScreen();
			UserInput input = ForgeUserInput.fromEvent(event);
			if (handler.onKeyboardKeyPressedPost(screen, input)) {
				event.setCanceled(true);
			}
		});

		subscriptions.register(ScreenEvent.KeyboardCharTypedEvent.Pre.class, event -> {
			Screen screen = event.getScreen();
			char codePoint = event.getCodePoint();
			int modifiers = event.getModifiers();
			if (handler.onKeyboardCharTypedPre(screen, codePoint, modifiers)) {
				event.setCanceled(true);
			}
		});
		subscriptions.register(ScreenEvent.KeyboardCharTypedEvent.Post.class, event -> {
			Screen screen = event.getScreen();
			char codePoint = event.getCodePoint();
			int modifiers = event.getModifiers();
			if (handler.onKeyboardCharTypedPost(screen, codePoint, modifiers)) {
				event.setCanceled(true);
			}
		});

		subscriptions.register(ScreenEvent.MouseClickedEvent.Pre.class, event ->
			ForgeUserInput.fromEvent(event)
				.ifPresent(input -> {
					Screen screen = event.getScreen();
					if (handler.onGuiMouseClicked(screen, input)) {
						event.setCanceled(true);
					}
				})
		);
		subscriptions.register(ScreenEvent.MouseReleasedEvent.Pre.class, event ->
			ForgeUserInput.fromEvent(event)
				.ifPresent(input -> {
					Screen screen = event.getScreen();
					if (handler.onGuiMouseReleased(screen, input)){
						event.setCanceled(true);
					}
				})
		);

		subscriptions.register(ScreenEvent.MouseScrollEvent.Pre.class, event -> {
			double mouseX = event.getMouseX();
			double mouseY = event.getMouseY();
			double scrollDelta = event.getScrollDelta();
			if (handler.onGuiMouseScroll(mouseX, mouseY, scrollDelta)) {
				event.setCanceled(true);
			}
		});
	}

	public static void registerGuiHandler(RuntimeEventSubscriptions subscriptions, GuiEventHandler guiEventHandler) {
		subscriptions.register(ScreenEvent.InitScreenEvent.Post.class, event -> {
			Screen screen = event.getScreen();
			guiEventHandler.onGuiInit(screen);
		});
		subscriptions.register(ScreenOpenEvent.class, event -> {
			Screen screen = event.getScreen();
			guiEventHandler.onGuiOpen(screen);
		});
		subscriptions.register(ScreenEvent.BackgroundDrawnEvent.class, event -> {
			Screen screen = event.getScreen();
			PoseStack poseStack = event.getPoseStack();
			guiEventHandler.onDrawBackgroundPost(screen, poseStack);
		});
		subscriptions.register(ContainerScreenEvent.DrawForeground.class, event -> {
			AbstractContainerScreen<?> containerScreen = event.getContainerScreen();
			PoseStack poseStack = event.getPoseStack();
			int mouseX = event.getMouseX();
			int mouseY = event.getMouseY();
			guiEventHandler.onDrawForeground(containerScreen, poseStack, mouseX, mouseY);
		});
		subscriptions.register(ScreenEvent.DrawScreenEvent.Post.class, event -> {
			Screen screen = event.getScreen();
			PoseStack poseStack = event.getPoseStack();
			int mouseX = event.getMouseX();
			int mouseY = event.getMouseY();
			guiEventHandler.onDrawScreenPost(screen, poseStack, mouseX, mouseY);
		});
		subscriptions.register(TickEvent.ClientTickEvent.class, event -> {
			if (event.phase == TickEvent.Phase.START) {
				guiEventHandler.onClientTick();
			}
		});
		subscriptions.register(ScreenEvent.PotionSizeEvent.class, event -> {
			if (guiEventHandler.renderCompactPotionIndicators()) {
				// Forcibly renders the potion indicators in compact mode.
				// This gives the ingredient list overlay more room to display ingredients.
				event.setResult(Event.Result.ALLOW);
			}
		});
	}
}
