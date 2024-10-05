package mezz.jei.gui.util;

import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.placement.VerticalAlignment;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.ImmutableSize2i;

public class AlignmentUtil {
	public static ImmutableRect2i align(ImmutableSize2i size, ImmutableRect2i availableArea, HorizontalAlignment horizontalAlignment, VerticalAlignment verticalAlignment) {
		final int width = size.width();
		final int height = size.height();
		final int x = availableArea.getX() + horizontalAlignment.getXPos(availableArea.width(), width);
		final int y = availableArea.getY() + verticalAlignment.getYPos(availableArea.height(), height);
		return new ImmutableRect2i(x, y, width, height);
	}
}
