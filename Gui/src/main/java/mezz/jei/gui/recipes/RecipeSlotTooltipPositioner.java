package mezz.jei.gui.recipes;

import mezz.jei.common.gui.TooltipPositioner;
import mezz.jei.common.gui.TooltipRenderHelper;
import mezz.jei.common.util.ImmutablePoint2i;
import mezz.jei.common.util.ImmutableRect2i;
import org.jetbrains.annotations.Nullable;

final class RecipeSlotTooltipPositioner implements TooltipPositioner {
	private static final int TOOLTIP_MARGIN = 4;
	private ImmutableRect2i tooltipArea = ImmutableRect2i.EMPTY;
	private @Nullable ImmutablePoint2i position;

	@Override
	public ImmutablePoint2i positionTooltip(int screenWidth, int screenHeight, int x, int y, int width, int height) {
		if (position == null) {
			ImmutablePoint2i initial = TooltipRenderHelper.defaultPosition(screenWidth, screenHeight, x, y, width, height);
			position = new ImmutablePoint2i(Math.max(TOOLTIP_MARGIN, initial.x()), initial.y());
		}
		tooltipArea = new ImmutableRect2i(position.x() - TOOLTIP_MARGIN, position.y() - TOOLTIP_MARGIN,
			width + 2 * TOOLTIP_MARGIN, height + 2 * TOOLTIP_MARGIN);
		return position;
	}

	public ImmutableRect2i getTooltipArea() {
		return tooltipArea;
	}
}
