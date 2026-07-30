package mezz.jei.fabric.events;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.gui.GuiGraphics;
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
		boolean allow(GuiGraphics guiGraphics);
	}

	public static final Event<DrawForeground> DRAW_FOREGROUND = EventFactory.createArrayBacked(DrawForeground.class, callbacks -> (screen, guiGraphics, mouseX, mouseY) -> {
		for (DrawForeground callback : callbacks) {
			callback.drawForeground(screen, guiGraphics, mouseX, mouseY);
		}
	});

	@Environment(EnvType.CLIENT)
	@FunctionalInterface
	public interface DrawForeground {
		void drawForeground(Screen screen, GuiGraphics guiGraphics, int mouseX, int mouseY);
	}

	public static final Event<DrawBackground> DRAW_BACKGROUND = EventFactory.createArrayBacked(DrawBackground.class, callbacks -> (screen, guiGraphics) -> {
		for (DrawBackground callback : callbacks) {
			callback.drawBackground(screen, guiGraphics);
		}
	});

	@Environment(EnvType.CLIENT)
	@FunctionalInterface
	public interface DrawBackground {
		void drawBackground(Screen screen, GuiGraphics guiGraphics);
	}
}
