package mezz.jei.gui.recipes;

import mezz.jei.common.util.ImmutableRect2i;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.jspecify.annotations.Nullable;

final class RecipeSlotTooltipPositioner implements ClientTooltipPositioner {
	private static final int TOOLTIP_MARGIN = 4;

	private ImmutableRect2i tooltipArea = ImmutableRect2i.EMPTY;
	private @Nullable Vector2i position;

	@Override
	public Vector2ic positionTooltip(
		int screenWidth,
		int screenHeight,
		int x,
		int y,
		int tooltipWidth,
		int tooltipHeight
	) {
		if (this.position == null) {
			Vector2ic defaultPosition = DefaultTooltipPositioner.INSTANCE.positionTooltip(
				screenWidth,
				screenHeight,
				x,
				y,
				tooltipWidth,
				tooltipHeight
			);
			this.position = new Vector2i(defaultPosition);
		}
		this.tooltipArea = new ImmutableRect2i(
			this.position.x() - TOOLTIP_MARGIN,
			this.position.y() - TOOLTIP_MARGIN,
			tooltipWidth + (2 * TOOLTIP_MARGIN),
			tooltipHeight + (2 * TOOLTIP_MARGIN)
		);
		return new Vector2i(this.position);
	}

	public ImmutableRect2i getTooltipArea() {
		return this.tooltipArea;
	}
}
