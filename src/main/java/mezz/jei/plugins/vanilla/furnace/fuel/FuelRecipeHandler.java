package mezz.jei.plugins.vanilla.furnace.fuel;

import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeType;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.plugins.vanilla.furnace.FurnaceRecipeType;

import javax.annotation.Nonnull;

public class FuelRecipeHandler implements IRecipeHandler {
	@Nonnull
	@Override
	public Class getRecipeClass() {
		return FuelRecipe.class;
	}

	@Nonnull
	@Override
	public Class<? extends IRecipeType> getRecipeTypeClass() {
		return FurnaceRecipeType.class;
	}

	@Override
	public IRecipeWrapper getRecipeWrapper(@Nonnull Object recipe) {
		return (FuelRecipe)recipe;
	}

}
