package mezz.jei.fabric.events;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

public class JeiScreenEvents {
	public static final Event<DrawForeground> DRAW_FOREGROUND =
		EventFactory.createArrayBacked(DrawForeground.class, callbacks -> (screen, guiGraphics, mouseX, mouseY) -> {
			for (DrawForeground callback : callbacks) {
				callback.drawForeground(screen, guiGraphics, mouseX, mouseY);
			}
		});

	public static final Event<AllowMouseDrag> ALLOW_MOUSE_DRAG =
		EventFactory.createArrayBacked(AllowMouseDrag.class, callbacks -> (screen, mouseX, mouseY, button, dragX, dragY) -> {
			for (AllowMouseDrag callback : callbacks) {
				if (!callback.allowMouseDrag(screen, mouseX, mouseY, button, dragX, dragY)) {
					return false;
				}
			}
			return true;
		});

	@Environment(EnvType.CLIENT)
	@FunctionalInterface
	public interface DrawForeground {
		void drawForeground(AbstractContainerScreen<?> screen, GuiGraphics guiGraphics, int mouseX, int mouseY);
	}

	@Environment(EnvType.CLIENT)
	@FunctionalInterface
	public interface AllowMouseDrag {
		boolean allowMouseDrag(Screen screen, double mouseX, double mouseY, int button, double dragX, double dragY);
	}
}
