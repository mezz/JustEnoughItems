package mezz.jei.fabric.events;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

public class JeiScreenEvents {
	public static final Event<AllowTooltip> ALLOW_TOOLTIP = EventFactory.createArrayBacked(
		AllowTooltip.class,
		callbacks -> () -> {
			for (AllowTooltip callback : callbacks) {
				if (!callback.allow()) {
					return false;
				}
			}
			return true;
		}
	);

	public static final Event<AfterRenderBackground> AFTER_RENDER_BACKGROUND =
		EventFactory.createArrayBacked(AfterRenderBackground.class, callbacks -> (screen, poseStack) -> {
			for (AfterRenderBackground callback : callbacks) {
				callback.afterRenderBackground(screen, poseStack);
			}
		});

	public static final Event<DrawForeground> DRAW_FOREGROUND =
		EventFactory.createArrayBacked(DrawForeground.class, callbacks -> (screen, poseStack, mouseX, mouseY) -> {
			for (DrawForeground callback : callbacks) {
				callback.drawForeground(screen, poseStack, mouseX, mouseY);
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
	public interface AfterRenderBackground {
		void afterRenderBackground(Screen screen, PoseStack poseStack);
	}

	@Environment(EnvType.CLIENT)
	@FunctionalInterface
	public interface AllowTooltip {
		boolean allow();
	}

	@Environment(EnvType.CLIENT)
	@FunctionalInterface
	public interface DrawForeground {
		void drawForeground(AbstractContainerScreen<?> screen, PoseStack poseStack, int mouseX, int mouseY);
	}

	@Environment(EnvType.CLIENT)
	@FunctionalInterface
	public interface AllowMouseDrag {
		boolean allowMouseDrag(Screen screen, double mouseX, double mouseY, int button, double dragX, double dragY);
	}
}
