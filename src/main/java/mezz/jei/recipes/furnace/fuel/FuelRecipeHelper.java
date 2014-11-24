package mezz.jei.recipes.furnace.fuel;

import mezz.jei.api.recipes.EnumRecipeType;
import mezz.jei.api.recipes.IRecipeGuiHelper;
import mezz.jei.api.recipes.IRecipeHelper;
import mezz.jei.api.recipes.IRecipeTypeKey;
import mezz.jei.api.recipes.IRecipeWrapper;

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
		return EnumRecipeType.FURNACE;
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
