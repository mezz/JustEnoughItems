package mezz.jei.common.gui.elements;

import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.helpers.IGuiHelper;
import net.minecraft.client.gui.GuiGraphics;

public class DrawableAnimatedRecipeArrow extends DrawableAnimated {
	private final IDrawableStatic blankArrow;

	public DrawableAnimatedRecipeArrow(IGuiHelper guiHelper, int ticksPerCycle) {
		super(guiHelper.getRecipeArrowFilled(), ticksPerCycle, StartDirection.LEFT, false);
		this.blankArrow = guiHelper.getRecipeArrow();
	}

	@Override
	public void draw(GuiGraphics guiGraphics, int xOffset, int yOffset) {
		this.blankArrow.draw(guiGraphics, xOffset, yOffset);
		super.draw(guiGraphics, xOffset, yOffset);
	}
}
