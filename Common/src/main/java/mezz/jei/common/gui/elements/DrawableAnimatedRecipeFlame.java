package mezz.jei.common.gui.elements;

import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.helpers.IGuiHelper;
import net.minecraft.client.gui.GuiGraphics;

public class DrawableAnimatedRecipeFlame extends DrawableAnimated {
	private final IDrawableStatic emptyFlame;

	public DrawableAnimatedRecipeFlame(IGuiHelper guiHelper, int ticksPerCycle) {
		super(guiHelper.getRecipeFlameFilled(), ticksPerCycle, StartDirection.TOP, true);
		this.emptyFlame = guiHelper.getRecipeFlameEmpty();
	}

	@Override
	public void draw(GuiGraphics guiGraphics, int xOffset, int yOffset) {
		this.emptyFlame.draw(guiGraphics, xOffset, yOffset);
		super.draw(guiGraphics, xOffset, yOffset);
	}
}
