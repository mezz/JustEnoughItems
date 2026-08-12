package mezz.jei.gui.recipes;

import mezz.jei.common.util.ImmutableRect2i;

final class RecipeSlotTooltipLayout {
	private static final int TOOLTIP_BORDER = 12;

	private RecipeSlotTooltipLayout() {
	}

	public static Result create(
		int screenWidth,
		int screenHeight,
		ImmutableRect2i sourceArea,
		int tooltipWidth,
		int tooltipHeight,
		int offsetX,
		int offsetY
	) {
		int rightX = sourceArea.getX() + sourceArea.getWidth() + TOOLTIP_BORDER + offsetX;
		int leftX = sourceArea.getX() - TOOLTIP_BORDER - tooltipWidth + offsetX;
		int tooltipX = rightX;
		if (rightX + tooltipWidth + TOOLTIP_BORDER > screenWidth) {
			tooltipX = leftX;
		}

		int maxX = Math.max(TOOLTIP_BORDER, screenWidth - tooltipWidth - TOOLTIP_BORDER);
		tooltipX = Math.clamp(tooltipX, TOOLTIP_BORDER, maxX);

		int tooltipY = sourceArea.getY() + TOOLTIP_BORDER + offsetY;
		int maxY = Math.max(TOOLTIP_BORDER, screenHeight - tooltipHeight - TOOLTIP_BORDER);
		tooltipY = Math.clamp(tooltipY, TOOLTIP_BORDER, maxY);

		ImmutableRect2i tooltipArea = new ImmutableRect2i(
			tooltipX - TOOLTIP_BORDER,
			tooltipY - TOOLTIP_BORDER,
			tooltipWidth + (2 * TOOLTIP_BORDER),
			tooltipHeight + (2 * TOOLTIP_BORDER)
		);
		return new Result(tooltipX, tooltipY, tooltipArea);
	}

	public record Result(int x, int y, ImmutableRect2i area) {
	}
}
