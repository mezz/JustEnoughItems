package mezz.jei.gui.elements;

import mezz.jei.common.util.ImmutableSize2i;

/** Uses the initial grab position so clicking inside a border does not make the panel jump. */
public record ResizeDrag(ResizeHandle handle, double mouseX, double mouseY, ImmutableSize2i size) {
	public ImmutableSize2i resize(double x, double y, boolean centeredX, boolean centeredY, ImmutableSize2i minimum, ImmutableSize2i maximum) {
		int width = size.width();
		int height = size.height();
		if (handle.horizontal()) {
			double delta = delta(x - mouseX, handle.left(), centeredX);
			width = (int) Math.round(width + delta);
			width = Math.clamp(width, Math.min(minimum.width(), maximum.width()), maximum.width());
		}
		if (handle.vertical()) {
			double delta = delta(y - mouseY, handle.top(), centeredY);
			height = (int) Math.round(height + delta);
			height = Math.clamp(height, Math.min(minimum.height(), maximum.height()), maximum.height());
		}
		return new ImmutableSize2i(width, height);
	}

	private static double delta(double delta, boolean reversed, boolean centered) {
		if (reversed) {
			delta = -delta;
		}
		if (centered) {
			delta *= 2;
		}
		return delta;
	}
}
