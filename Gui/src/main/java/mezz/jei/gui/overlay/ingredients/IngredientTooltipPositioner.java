package mezz.jei.gui.overlay.ingredients;

import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.gui.TooltipPositioner;
import mezz.jei.common.util.ImmutablePoint2i;

/** Places keyboard-selected ingredient tooltips beside their slot within the screen bounds. */
record IngredientTooltipPositioner(ImmutableRect2i slot) implements TooltipPositioner {
	private static final int PADDING = 4;

	@Override
	public ImmutablePoint2i positionTooltip(int screenWidth, int screenHeight, int x, int y, int width, int height) {
		int tooltipX = Math.max(PADDING, Math.min(slot.getX(), screenWidth - width - PADDING));
		int tooltipY = slot.getY() + slot.getHeight() + PADDING;
		if (tooltipY + height + PADDING > screenHeight) {
			tooltipY = slot.getY() - height - PADDING;
		}
		tooltipY = Math.max(PADDING, Math.min(tooltipY, screenHeight - height - PADDING));
		return new ImmutablePoint2i(tooltipX, tooltipY);
	}
}
