package mezz.jei.common.gui.elements;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.helpers.IGuiHelper;

public class DrawableAnimatedRecipeArrow extends DrawableAnimated {
	private final IDrawableStatic blankArrow;

	public DrawableAnimatedRecipeArrow(IGuiHelper guiHelper, int ticksPerCycle) {
		super(guiHelper.getRecipeArrowFilled(), ticksPerCycle, StartDirection.LEFT, false);
		this.blankArrow = guiHelper.getRecipeArrow();
	}

	@Override
	public void draw(PoseStack poseStack, int xOffset, int yOffset) {
		this.blankArrow.draw(poseStack, xOffset, yOffset);
		super.draw(poseStack, xOffset, yOffset);
	}
}
