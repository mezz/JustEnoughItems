package mezz.jei.common.util;

import net.minecraft.client.gui.GuiGraphicsExtractor;

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

	public void draw(GuiGraphicsExtractor guiGraphics) {
		for (Rect rect : rects.values()) {
			ImmutableRect2i rect1 = rect.rect;
			guiGraphics.fill(
				rect1.getX(),
				rect1.getY(),
				rect1.getX() + rect1.getWidth(),
				rect1.getY() + rect1.getHeight(),
				rect.color
			);
		}
	}
}
