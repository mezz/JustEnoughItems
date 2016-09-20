package mezz.jei.plugins.vanilla.furnace;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.util.Translator;

public class FurnaceFuelCategory extends FurnaceRecipeCategory<FuelRecipe> {
	private final IDrawable background;
	private final String localizedName;

	public FurnaceFuelCategory(IGuiHelper guiHelper) {
		super(guiHelper);
		background = guiHelper.createDrawable(backgroundLocation, 55, 38, 18, 32, 0, 0, 0, 80);
		localizedName = Translator.translateToLocal("gui.jei.category.fuel");
	}

	@Override
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public String getUid() {
		return VanillaRecipeCategoryUid.FUEL;
	}

	@Override
	public String getTitle() {
		return localizedName;
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, FuelRecipe recipeWrapper) {
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();

		guiItemStacks.init(fuelSlot, true, 0, 14);
		guiItemStacks.setFromRecipe(fuelSlot, recipeWrapper.getInputs());
	}
}
