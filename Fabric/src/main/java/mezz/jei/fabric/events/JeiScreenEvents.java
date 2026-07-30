package mezz.jei.fabric.events;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

public class JeiScreenEvents {
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
		void drawForeground(AbstractContainerScreen<?> screen, GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY);
	}

	public static final Event<DrawBackground> DRAW_BACKGROUND = createDrawBackgroundEvent();

	private static Event<DrawBackground> createDrawBackgroundEvent() {
		return EventFactory.createArrayBacked(DrawBackground.class, JeiScreenEvents::createDrawBackgroundInvoker);
	}

	private static DrawBackground createDrawBackgroundInvoker(DrawBackground[] callbacks) {
		return (screen, guiGraphics, mouseX, mouseY, partialTicks) -> {
			for (DrawBackground callback : callbacks) {
				callback.drawBackground(screen, guiGraphics, mouseX, mouseY, partialTicks);
			}
		};
	}

	@Environment(EnvType.CLIENT)
	@FunctionalInterface
	public interface DrawBackground {
		void drawBackground(Screen screen, GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks);
	}
}
