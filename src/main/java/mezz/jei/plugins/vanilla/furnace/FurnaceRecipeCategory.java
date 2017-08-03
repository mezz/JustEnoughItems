package mezz.jei.plugins.vanilla.furnace;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawableAnimated;
import mezz.jei.api.gui.IDrawableStatic;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.config.Constants;

public abstract class FurnaceRecipeCategory<T extends IRecipeWrapper> implements IRecipeCategory<T> {
	protected static final int inputSlot = 0;
	protected static final int fuelSlot = 1;
	protected static final int outputSlot = 2;

	protected final IDrawableStatic staticFlame;
	protected final IDrawableAnimated animatedFlame;
	protected final IDrawableAnimated arrow;

	public FurnaceRecipeCategory(IGuiHelper guiHelper) {
		staticFlame = guiHelper.createDrawable(Constants.RECIPE_GUI_VANILLA, 82, 114, 14, 14);
		animatedFlame = guiHelper.createAnimatedDrawable(staticFlame, 300, IDrawableAnimated.StartDirection.TOP, true);

		IDrawableStatic arrowDrawable = guiHelper.createDrawable(Constants.RECIPE_GUI_VANILLA, 82, 128, 24, 17);
		this.arrow = guiHelper.createAnimatedDrawable(arrowDrawable, 200, IDrawableAnimated.StartDirection.LEFT, false);
	}
}
