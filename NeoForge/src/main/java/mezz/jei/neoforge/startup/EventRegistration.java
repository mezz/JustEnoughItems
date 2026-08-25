package mezz.jei.neoforge.startup;

import mezz.jei.gui.events.GuiEventHandler;
import mezz.jei.gui.input.ClientInputHandler;
import mezz.jei.gui.input.GuiTextFieldFilter;
import mezz.jei.gui.input.UserInput;
import mezz.jei.gui.startup.JeiEventHandlers;
import mezz.jei.neoforge.events.JeiScreenRenderForegroundEvent;
import mezz.jei.neoforge.events.RuntimeEventSubscriptions;
import mezz.jei.neoforge.input.ForgeUserInput;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ContainerScreenEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import org.joml.Matrix3x2fStack;

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
			CharacterEvent characterEvent = event.getCharacterEvent();
			if (handler.onKeyboardCharTypedPre(screen, characterEvent)) {
				event.setCanceled(true);
			}
		});
		subscriptions.register(ScreenEvent.CharacterTyped.Post.class, event -> {
			Screen screen = event.getScreen();
			CharacterEvent characterEvent = event.getCharacterEvent();
			handler.onKeyboardCharTypedPost(screen, characterEvent);
		});

		subscriptions.register(ScreenEvent.Preedit.Pre.class, event -> {
			Screen screen = event.getScreen();
			if (screen.getFocused() instanceof GuiTextFieldFilter searchField && searchField.isFocused()) {
				searchField.preeditUpdated(event.getPreeditEvent());
				event.setCanceled(true);
			}
		});

		subscriptions.register(ScreenEvent.MouseButtonPressed.Pre.class, event -> {
			ForgeUserInput.fromEvent(event)
				.ifPresent(input -> {
					Screen screen = event.getScreen();
					if (handler.onGuiMouseClicked(screen, input)) {
						event.setCanceled(true);
					}
				});
		});
		subscriptions.register(ScreenEvent.MouseButtonReleased.Pre.class, event -> {
			ForgeUserInput.fromEvent(event)
				.ifPresent(input -> {
					Screen screen = event.getScreen();
					if (handler.onGuiMouseReleased(screen, input)) {
						event.setCanceled(true);
					}
				});
		});

		subscriptions.register(ScreenEvent.MouseScrolled.Pre.class, event -> {
			double mouseX = event.getMouseX();
			double mouseY = event.getMouseY();
			double scrollDeltaX = event.getScrollDeltaX();
			double scrollDeltaY = event.getScrollDeltaY();
			if (handler.onGuiMouseScroll(mouseX, mouseY, scrollDeltaX, scrollDeltaY)) {
				event.setCanceled(true);
			}
		});

		subscriptions.register(ScreenEvent.MouseDragged.Pre.class, event -> {
			Screen screen = event.getScreen();
			if (handler.onGuiMouseDragged(screen, event.getMouseButtonEvent(), event.getDragX(), event.getDragY())) {
				event.setCanceled(true);
			}
		});
	}

	public static void registerGuiHandler(
		RuntimeEventSubscriptions subscriptions,
		GuiEventHandler guiEventHandler
	) {
		subscriptions.register(ClientTickEvent.Post.class, event -> {
			if (Minecraft.getInstance().screen != null) {
				guiEventHandler.onClientTick();
			}
		});
		subscriptions.register(ScreenEvent.Init.Post.class, event -> {
			Screen screen = event.getScreen();
			guiEventHandler.onGuiInit(screen);
		});
		subscriptions.register(ScreenEvent.Opening.class, event -> {
			Screen screen = event.getScreen();
			guiEventHandler.onGuiOpen(screen);
		});
		subscriptions.register(EventPriority.LOWEST, ContainerScreenEvent.Render.Foreground.class, event -> {
			Screen screen = event.getContainerScreen();
			var guiGraphics = event.getGuiGraphics();
			int mouseX = event.getMouseX();
			int mouseY = event.getMouseY();
			guiGraphics.nextStratum();
			runWithIdentityPose(guiGraphics, () -> {
				guiEventHandler.drawForScreenForeground(screen, guiGraphics, mouseX, mouseY);
			});
		});
		subscriptions.register(EventPriority.HIGHEST, ScreenEvent.Render.Background.class, event -> {
			Screen screen = event.getScreen();
			var guiGraphics = event.getGuiGraphics();
			runWithIdentityPose(guiGraphics, () -> {
				guiEventHandler.drawForScreenBackground(screen, guiGraphics);
			});
		});
		subscriptions.register(EventPriority.LOWEST, JeiScreenRenderForegroundEvent.class, event -> {
			Screen screen = event.getScreen();
			var guiGraphics = event.getGuiGraphics();
			int mouseX = event.getMouseX();
			int mouseY = event.getMouseY();
			guiEventHandler.drawForScreenForeground(screen, guiGraphics, mouseX, mouseY);
		});
		subscriptions.register(ScreenEvent.RenderInventoryMobEffects.class, event -> {
			if (guiEventHandler.renderCompactPotionIndicators()) {
				// Forcibly renders the potion indicators in compact mode.
				// This gives the ingredient list overlay more room to display ingredients.
				event.setCompact(true);
			}
		});
	}

	private static void runWithIdentityPose(GuiGraphicsExtractor guiGraphics, Runnable runnable) {
		Matrix3x2fStack pose = guiGraphics.pose();
		pose.pushMatrix();
		pose.identity();
		try {
			runnable.run();
		} finally {
			pose.popMatrix();
		}
	}
}
