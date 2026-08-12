package mezz.jei.gui.recipes;

import mezz.jei.common.util.ImmutableRect2i;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import org.joml.Vector2i;
import org.joml.Vector2ic;

final class RecipeSlotTooltipPositioner implements ClientTooltipPositioner {
	private final ImmutableRect2i sourceArea;
	private final int anchorX;
	private final int anchorY;
	private ImmutableRect2i tooltipArea = ImmutableRect2i.EMPTY;

	public RecipeSlotTooltipPositioner(ImmutableRect2i sourceArea) {
		this.sourceArea = sourceArea;
		this.anchorX = sourceArea.getX() + sourceArea.getWidth();
		this.anchorY = sourceArea.getY();
	}

	@Override
	public Vector2ic positionTooltip(
		int screenWidth,
		int screenHeight,
		int x,
		int y,
		int tooltipWidth,
		int tooltipHeight
	) {
		int offsetX = x - this.anchorX;
		int offsetY = y - this.anchorY;
		RecipeSlotTooltipLayout.Result layout = RecipeSlotTooltipLayout.create(
			screenWidth,
			screenHeight,
			this.sourceArea,
			tooltipWidth,
			tooltipHeight,
			offsetX,
			offsetY
		);
		this.tooltipArea = layout.area();
		return new Vector2i(layout.x(), layout.y());
	}

	public int getAnchorX() {
		return this.anchorX;
	}

	public int getAnchorY() {
		return this.anchorY;
	}

	public ImmutableRect2i getTooltipArea() {
		return this.tooltipArea;
	}
}
