package mezz.jei.gui.elements;

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import mezz.jei.common.util.ImmutableRect2i;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** The edges of a panel that can be dragged together. */
public record ResizeHandle(boolean left, boolean right, boolean top, boolean bottom) {
	public static final ResizeHandle NONE = new ResizeHandle(false, false, false, false);
	public static final int SIZE = 5;

	public static ResizeHandle at(ImmutableRect2i area, double mouseX, double mouseY) {
		if (area.isEmpty() || !area.contains(mouseX, mouseY)) {
			return NONE;
		}
		return new ResizeHandle(
			mouseX < area.x() + SIZE,
			mouseX >= area.x() + area.width() - SIZE,
			mouseY < area.y() + SIZE,
			mouseY >= area.y() + area.height() - SIZE
		);
	}

	public boolean horizontal() {
		return left || right;
	}

	public boolean vertical() {
		return top || bottom;
	}

	public void requestCursor(GuiGraphicsExtractor graphics) {
		if (horizontal() && vertical()) {
			graphics.requestCursor(CursorTypes.RESIZE_ALL);
		} else if (horizontal()) {
			graphics.requestCursor(CursorTypes.RESIZE_EW);
		} else if (vertical()) {
			graphics.requestCursor(CursorTypes.RESIZE_NS);
		}
	}

	public boolean isPresent() {
		return horizontal() || vertical();
	}
}
