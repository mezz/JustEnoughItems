package mezz.jei.gui.elements;

import mezz.jei.common.util.ImmutableRect2i;

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

	public boolean isPresent() {
		return horizontal() || vertical();
	}
}
