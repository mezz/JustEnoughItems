package mezz.jei.gui.util;

import mezz.jei.common.util.HorizontalAlignment;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.ImmutableSize2i;
import mezz.jei.common.util.VerticalAlignment;

public class AlignmentUtil {
    public static ImmutableRect2i align(ImmutableSize2i size, ImmutableRect2i availableArea, HorizontalAlignment horizontalAlignment, VerticalAlignment verticalAlignment) {
        final int width = size.getWidth();
        final int x = switch (horizontalAlignment) {
            case LEFT -> availableArea.getX();
            case CENTER -> availableArea.getX() + ((availableArea.getWidth() - width) / 2);
            case RIGHT -> availableArea.getX() + (availableArea.getWidth() - width);
        };

        final int height = size.getHeight();
        final int y = switch (verticalAlignment) {
            case TOP -> availableArea.getY();
            case CENTER -> availableArea.getY() + ((availableArea.getHeight() - height) / 2);
            case BOTTOM -> availableArea.getY() + (availableArea.getHeight() - height);
        };

        return new ImmutableRect2i(x, y, width, height);
    }
}
