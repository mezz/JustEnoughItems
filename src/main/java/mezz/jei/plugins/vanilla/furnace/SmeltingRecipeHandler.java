package mezz.jei.plugins.vanilla.furnace;

import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;

import javax.annotation.Nonnull;

public class SmeltingRecipeHandler implements IRecipeHandler {

	@Nonnull
	@Override
	public Class getRecipeClass() {
		return SmeltingRecipe.class;
	}

	@Nonnull
	@Override
	public Class<? extends IRecipeCategory> getRecipeCategoryClass() {
		return FurnaceRecipeCategory.class;
	}

	@Override
	public IRecipeWrapper getRecipeWrapper(@Nonnull Object recipe) {
		return (SmeltingRecipe)recipe;
	}

}
