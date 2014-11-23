package mezz.jei.recipes.furnace.fuel;

import mezz.jei.api.recipes.IRecipeGuiHelper;
import mezz.jei.api.recipes.IRecipeHelper;
import mezz.jei.api.recipes.IRecipeType;
import mezz.jei.api.recipes.IRecipeWrapper;
import mezz.jei.api.recipes.RecipeType;

import javax.annotation.Nonnull;

public class FuelRecipeHelper implements IRecipeHelper {
	@Nonnull
	@Override
	public Class getRecipeClass() {
		return FuelRecipe.class;
	}

	@Nonnull
	@Override
	public IRecipeType getRecipeType() {
		return RecipeType.FURNACE;
	}

	@Nonnull
	@Override
	public IRecipeGuiHelper createGuiHelper() {
		return new FuelRecipeGui();
	}

	@Override
	public IRecipeWrapper getRecipeWrapper(@Nonnull Object recipe) {
		return (FuelRecipe)recipe;
	}

}
