package mezz.jei.plugins.vanilla.furnace;

import javax.annotation.Nonnull;

import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;

public class FuelRecipeHandler implements IRecipeHandler<FuelRecipe> {
	@Override
	@Nonnull
	public Class<FuelRecipe> getRecipeClass() {
		return FuelRecipe.class;
	}

	@Override
	@Nonnull
	public Class<? extends IRecipeCategory> getRecipeCategoryClass() {
		return FurnaceRecipeCategory.class;
	}

	@Override
	@Nonnull
	public IRecipeWrapper getRecipeWrapper(@Nonnull FuelRecipe recipe) {
		return recipe;
	}

	@Override
	public boolean isRecipeValid(@Nonnull FuelRecipe recipe) {
		return recipe.getInputs().size() > 0 && recipe.getOutputs().size() == 0;
	}
}
