package mezz.jei.common.gui;

import mezz.jei.common.util.ImmutablePoint2i;

/** Positions a tooltip inside the scaled screen. */
@FunctionalInterface
public interface TooltipPositioner {
	ImmutablePoint2i positionTooltip(int screenWidth, int screenHeight, int x, int y, int width, int height);
}
