package mezz.jei.gui.recipes;

import mezz.jei.api.gui.drawable.IScalableDrawable;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.JeiGuiColors;
import mezz.jei.common.gui.JeiGuiColors.GuiColor;
import mezz.jei.common.gui.JeiTooltip;
import mezz.jei.common.util.ImmutableRect2i;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class PinnedTooltipRenderer {
	private static final int BACKGROUND_PADDING = 2;

	private final RecipeSlotTooltipPositioner positioner = new RecipeSlotTooltipPositioner();
	private final IScalableDrawable background = Internal.getTextures().getInteractiveIngredientTooltipBackground();
	private final int anchorX;
	private final int anchorY;

	public PinnedTooltipRenderer(int anchorX, int anchorY) {
		this.anchorX = anchorX;
		this.anchorY = anchorY;
	}

	public boolean isMouseOver(double mouseX, double mouseY) {
		return getArea().contains(mouseX, mouseY);
	}

	public void draw(GuiGraphicsExtractor guiGraphics, JeiTooltip tooltip) {
		ImmutableRect2i area = getArea();
		guiGraphics.nextStratum();
		guiGraphics.fill(
			0,
			0,
			guiGraphics.guiWidth(),
			guiGraphics.guiHeight(),
			JeiGuiColors.getColor(GuiColor.INTERACTIVE_INGREDIENT_TOOLTIP_SCREEN_DIM)
		);
		guiGraphics.nextStratum();
		if (!area.isEmpty()) {
			this.background.draw(
				guiGraphics,
				area.x(),
				area.y(),
				area.width(),
				area.height()
			);
		}
		guiGraphics.nextStratum();
		tooltip.draw(guiGraphics, this.anchorX, this.anchorY, this.positioner);
	}

	private ImmutableRect2i getArea() {
		ImmutableRect2i tooltipArea = this.positioner.getTooltipArea();
		if (tooltipArea.isEmpty()) {
			return ImmutableRect2i.EMPTY;
		}
		return tooltipArea.expandBy(BACKGROUND_PADDING);
	}
}
