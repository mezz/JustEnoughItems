package mezz.jei.recipe.furnace.fuel;

import mezz.jei.api.recipe.type.EnumRecipeTypeKey;
import mezz.jei.api.recipe.IRecipeGuiHelper;
import mezz.jei.api.recipe.IRecipeHelper;
import mezz.jei.api.recipe.type.IRecipeTypeKey;
import mezz.jei.api.recipe.IRecipeWrapper;

import javax.annotation.Nonnull;

public class FuelRecipeHelper implements IRecipeHelper {
	@Nonnull
	@Override
	public Class getRecipeClass() {
		return FuelRecipe.class;
	}

	@Nonnull
	@Override
	public IRecipeTypeKey getRecipeTypeKey() {
		return EnumRecipeTypeKey.FURNACE;
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
