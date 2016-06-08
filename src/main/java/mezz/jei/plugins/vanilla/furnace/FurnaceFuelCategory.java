package mezz.jei.plugins.vanilla.furnace;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.util.Translator;

import javax.annotation.Nonnull;

public class FurnaceFuelCategory extends FurnaceRecipeCategory<FuelRecipe> {
	@Nonnull
	private final IDrawable background;
	@Nonnull
	private final String localizedName;

	public FurnaceFuelCategory(IGuiHelper guiHelper) {
		super(guiHelper);
		background = guiHelper.createDrawable(backgroundLocation, 55, 38, 18, 32, 0, 0, 0, 80);
		localizedName = Translator.translateToLocal("gui.jei.category.fuel");
	}

	@Override
	@Nonnull
	public IDrawable getBackground() {
		return background;
	}

	@Nonnull
	@Override
	public String getUid() {
		return VanillaRecipeCategoryUid.FUEL;
	}

	@Nonnull
	@Override
	public String getTitle() {
		return localizedName;
	}

	@Override
	public void setRecipe(@Nonnull IRecipeLayout recipeLayout, @Nonnull FuelRecipe recipeWrapper) {
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();

		guiItemStacks.init(fuelSlot, true, 0, 14);
		guiItemStacks.setFromRecipe(fuelSlot, recipeWrapper.getInputs());
	}
}
