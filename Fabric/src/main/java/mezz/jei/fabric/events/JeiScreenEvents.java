package mezz.jei.fabric.events;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

public class JeiScreenEvents {
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


    @Environment(EnvType.CLIENT)
    @FunctionalInterface
    public interface AfterRenderBackground {
        void afterRenderBackground(Screen screen, PoseStack poseStack);
    }

    @Environment(EnvType.CLIENT)
    @FunctionalInterface
    public interface DrawForeground {
        void drawForeground(AbstractContainerScreen<?> screen, PoseStack poseStack, int mouseX, int mouseY);
    }
}
