package mezz.jei.common.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;

import java.util.HashMap;
import java.util.Map;

public final class RectDebugger {
    public static final RectDebugger INSTANCE = new RectDebugger();

    private record Rect(ImmutableRect2i rect, int color) {

    }

    private final Map<String, Rect> rects = new HashMap<>();

    private RectDebugger() {

    }

    public void add(ImmutableRect2i rect, int color, String id) {
        this.rects.put(id, new Rect(rect, color));
    }

    public void draw(GuiGraphics guiGraphics) {
        RenderSystem.disableDepthTest();

        for (Rect rect : rects.values()) {
            ImmutableRect2i rect1 = rect.rect;
            guiGraphics.fill(
                RenderType.guiOverlay(),
                rect1.getX(),
                rect1.getY(),
                rect1.getX() + rect1.getWidth(),
                rect1.getY() + rect1.getHeight(),
                rect.color
            );
        }
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }
}
