package mezz.jei.fabric.events;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;

public class JeiScreenEvents {
	public static final Event<AllowDeferredTooltip> ALLOW_DEFERRED_TOOLTIP = EventFactory.createArrayBacked(
		AllowDeferredTooltip.class,
		callbacks -> guiGraphics -> {
			for (AllowDeferredTooltip callback : callbacks) {
				if (!callback.allow(guiGraphics)) {
					return false;
				}
			}
			return true;
		}
	);

	@Environment(EnvType.CLIENT)
	@FunctionalInterface
	public interface AllowDeferredTooltip {
		boolean allow(GuiGraphicsExtractor guiGraphics);
	}

	public static final Event<DrawForeground> DRAW_FOREGROUND = createDrawForegroundEvent();

	private static Event<DrawForeground> createDrawForegroundEvent() {
		return EventFactory.createArrayBacked(DrawForeground.class, JeiScreenEvents::createDrawForegroundInvoker);
	}

	private static DrawForeground createDrawForegroundInvoker(DrawForeground[] callbacks) {
		return (screen, guiGraphics, mouseX, mouseY) -> {
			for (DrawForeground callback : callbacks) {
				callback.drawForeground(screen, guiGraphics, mouseX, mouseY);
			}
		};
	}

	@Environment(EnvType.CLIENT)
	@FunctionalInterface
	public interface DrawForeground {
		void drawForeground(Screen screen, GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY);
	}

	public static final Event<DrawBackground> DRAW_BACKGROUND = createDrawBackgroundEvent();

	private static Event<DrawBackground> createDrawBackgroundEvent() {
		return EventFactory.createArrayBacked(DrawBackground.class, JeiScreenEvents::createDrawBackgroundInvoker);
	}

	private static DrawBackground createDrawBackgroundInvoker(DrawBackground[] callbacks) {
		return (screen, guiGraphics) -> {
			for (DrawBackground callback : callbacks) {
				callback.drawBackground(screen, guiGraphics);
			}
		};
	}

	@Environment(EnvType.CLIENT)
	@FunctionalInterface
	public interface DrawBackground {
		void drawBackground(Screen screen, GuiGraphicsExtractor guiGraphics);
	}
}
