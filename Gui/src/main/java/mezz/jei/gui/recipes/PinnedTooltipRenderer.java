package mezz.jei.gui.recipes;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.drawable.IScalableDrawable;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.JeiGuiColors;
import mezz.jei.common.gui.JeiGuiColors.GuiColor;
import mezz.jei.common.gui.JeiTooltip;
import mezz.jei.common.util.ImmutableRect2i;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;

public final class PinnedTooltipRenderer {
	private static final int TOOLTIP_FOREGROUND_Z = 300;
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

	public void draw(PoseStack poseStack, JeiTooltip tooltip) {
		ImmutableRect2i area = getArea();
		var window = Minecraft.getInstance().getWindow();
		poseStack.pushPose();
		try {
			poseStack.translate(0, 0, TOOLTIP_FOREGROUND_Z);
			GuiComponent.fill(poseStack, 0, 0, window.getGuiScaledWidth(), window.getGuiScaledHeight(),
				JeiGuiColors.getColor(GuiColor.INTERACTIVE_INGREDIENT_TOOLTIP_SCREEN_DIM));
			if (!area.isEmpty()) {
				background.draw(poseStack, area.x(), area.y(), area.width(), area.height());
			}
		} finally {
			poseStack.popPose();
		}
		// Native 1.19 adds another 400 Z; keep parent items and nested tooltips inside its GUI projection.
		tooltip.draw(poseStack, anchorX, anchorY, positioner);
	}

	public ImmutableRect2i getArea() {
		ImmutableRect2i area = positioner.getTooltipArea();
		if (area.isEmpty()) {
			return ImmutableRect2i.EMPTY;
		}
		return area.expandBy(BACKGROUND_PADDING);
	}
}
