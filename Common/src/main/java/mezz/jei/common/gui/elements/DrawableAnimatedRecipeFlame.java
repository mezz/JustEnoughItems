package mezz.jei.common.gui.elements;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.helpers.IGuiHelper;

public class DrawableAnimatedRecipeFlame extends DrawableAnimated {
	private final IDrawableStatic emptyFlame;

	public DrawableAnimatedRecipeFlame(IGuiHelper guiHelper, int ticksPerCycle) {
		super(guiHelper.getRecipeFlameFilled(), ticksPerCycle, StartDirection.TOP, true);
		this.emptyFlame = guiHelper.getRecipeFlameEmpty();
	}

	@Override
	public void draw(PoseStack poseStack, int xOffset, int yOffset) {
		this.emptyFlame.draw(poseStack, xOffset, yOffset);
		super.draw(poseStack, xOffset, yOffset);
	}
}
