package mezz.jei.fabric.events;

import net.minecraft.client.gui.GuiGraphics;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

public class JeiScreenEvents {
    public static final Event<AfterRenderBackground> AFTER_RENDER_BACKGROUND =
        EventFactory.createArrayBacked(AfterRenderBackground.class, callbacks -> (screen, guiGraphics) -> {
            for (AfterRenderBackground callback : callbacks) {
                callback.afterRenderBackground(screen, guiGraphics);
            }
        });

    public static final Event<DrawForeground> DRAW_FOREGROUND =
        EventFactory.createArrayBacked(DrawForeground.class, callbacks -> (screen, guiGraphics, mouseX, mouseY) -> {
            for (DrawForeground callback : callbacks) {
                callback.drawForeground(screen, guiGraphics, mouseX, mouseY);
            }
        });


    @Environment(EnvType.CLIENT)
    @FunctionalInterface
    public interface AfterRenderBackground {
        void afterRenderBackground(Screen screen, GuiGraphics guiGraphics);
    }

    @Environment(EnvType.CLIENT)
    @FunctionalInterface
    public interface DrawForeground {
        void drawForeground(AbstractContainerScreen<?> screen, GuiGraphics guiGraphics, int mouseX, int mouseY);
    }
}
