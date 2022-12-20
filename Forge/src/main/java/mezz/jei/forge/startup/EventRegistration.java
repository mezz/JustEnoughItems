package mezz.jei.forge.startup;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.gui.events.GuiEventHandler;
import mezz.jei.gui.input.ClientInputHandler;
import mezz.jei.gui.input.UserInput;
import mezz.jei.gui.startup.JeiEventHandlers;
import mezz.jei.forge.events.RuntimeEventSubscriptions;
import mezz.jei.forge.input.ForgeUserInput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraftforge.client.event.ContainerScreenEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;

public class EventRegistration {
	public static void registerEvents(RuntimeEventSubscriptions subscriptions, JeiEventHandlers eventHandlers) {
		ClientInputHandler clientInputHandler = eventHandlers.clientInputHandler();
		registerClientInputHandler(subscriptions, clientInputHandler);

		GuiEventHandler guiEventHandler = eventHandlers.guiEventHandler();
		registerGuiHandler(subscriptions, guiEventHandler);
	}

	private static void registerClientInputHandler(RuntimeEventSubscriptions subscriptions, ClientInputHandler handler) {
		subscriptions.register(ScreenEvent.Init.Post.class, event -> handler.onInitGui());

		subscriptions.register(ScreenEvent.KeyPressed.Pre.class, event -> {
			Screen screen = event.getScreen();
			UserInput input = ForgeUserInput.fromEvent(event);
			if (handler.onKeyboardKeyPressedPre(screen, input)) {
				event.setCanceled(true);
			}
		});
		subscriptions.register(ScreenEvent.KeyPressed.Post.class, event -> {
			Screen screen = event.getScreen();
			UserInput input = ForgeUserInput.fromEvent(event);
			if (handler.onKeyboardKeyPressedPost(screen, input)) {
				event.setCanceled(true);
			}
		});

		subscriptions.register(ScreenEvent.CharacterTyped.Pre.class, event -> {
			Screen screen = event.getScreen();
			char codePoint = event.getCodePoint();
			int modifiers = event.getModifiers();
			if (handler.onKeyboardCharTypedPre(screen, codePoint, modifiers)) {
				event.setCanceled(true);
			}
		});
		subscriptions.register(ScreenEvent.CharacterTyped.Post.class, event -> {
			Screen screen = event.getScreen();
			char codePoint = event.getCodePoint();
			int modifiers = event.getModifiers();
			handler.onKeyboardCharTypedPost(screen, codePoint, modifiers);
		});

		subscriptions.register(ScreenEvent.MouseButtonPressed.Pre.class, event ->
			ForgeUserInput.fromEvent(event)
				.ifPresent(input -> {
					Screen screen = event.getScreen();
					if (handler.onGuiMouseClicked(screen, input)) {
						event.setCanceled(true);
					}
				})
		);
		subscriptions.register(ScreenEvent.MouseButtonReleased.Pre.class, event ->
			ForgeUserInput.fromEvent(event)
				.ifPresent(input -> {
					Screen screen = event.getScreen();
					if (handler.onGuiMouseReleased(screen, input)){
						event.setCanceled(true);
					}
				})
		);

		subscriptions.register(ScreenEvent.MouseScrolled.Pre.class, event -> {
			double mouseX = event.getMouseX();
			double mouseY = event.getMouseY();
			double scrollDelta = event.getScrollDelta();
			if (handler.onGuiMouseScroll(mouseX, mouseY, scrollDelta)) {
				event.setCanceled(true);
			}
		});
	}

	public static void registerGuiHandler(RuntimeEventSubscriptions subscriptions, GuiEventHandler guiEventHandler) {
		subscriptions.register(ScreenEvent.Init.Post.class, event -> {
			Screen screen = event.getScreen();
			guiEventHandler.onGuiInit(screen);
		});
		subscriptions.register(ScreenEvent.Opening.class, event -> {
			Screen screen = event.getScreen();
			guiEventHandler.onGuiOpen(screen);
		});
		subscriptions.register(ScreenEvent.BackgroundRendered.class, event -> {
			Screen screen = event.getScreen();
			PoseStack poseStack = event.getPoseStack();
			guiEventHandler.onDrawBackgroundPost(screen, poseStack);
		});
		subscriptions.register(ContainerScreenEvent.Render.Foreground.class, event -> {
			AbstractContainerScreen<?> containerScreen = event.getContainerScreen();
			PoseStack poseStack = event.getPoseStack();
			int mouseX = event.getMouseX();
			int mouseY = event.getMouseY();
			guiEventHandler.onDrawForeground(containerScreen, poseStack, mouseX, mouseY);
		});
		subscriptions.register(ScreenEvent.Render.Post.class, event -> {
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
		subscriptions.register(ScreenEvent.RenderInventoryMobEffects.class, event -> {
			if (guiEventHandler.renderCompactPotionIndicators()) {
				// Forcibly renders the potion indicators in compact mode.
				// This gives the ingredient list overlay more room to display ingredients.
				event.setCompact(true);
			}
		});
	}
}
