package mezz.jei.plugins.vanilla.furnace.smelting;

import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeType;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.plugins.vanilla.furnace.FurnaceRecipeType;

import javax.annotation.Nonnull;

public class SmeltingRecipeHandler implements IRecipeHandler {

	@Nonnull
	@Override
	public Class getRecipeClass() {
		return SmeltingRecipe.class;
	}

	@Nonnull
	@Override
	public Class<? extends IRecipeType> getRecipeTypeClass() {
		return FurnaceRecipeType.class;
	}

	@Override
	public IRecipeWrapper getRecipeWrapper(@Nonnull Object recipe) {
		return (SmeltingRecipe)recipe;
	}

}
