package mezz.jei.plugins.vanilla.furnace;

import javax.annotation.Nonnull;

import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;

public class SmeltingRecipeHandler implements IRecipeHandler<SmeltingRecipe> {

	@Override
	@Nonnull
	public Class<SmeltingRecipe> getRecipeClass() {
		return SmeltingRecipe.class;
	}

	@Override
	@Nonnull
	public Class<? extends IRecipeCategory> getRecipeCategoryClass() {
		return FurnaceRecipeCategory.class;
	}

	@Override
	@Nonnull
	public IRecipeWrapper getRecipeWrapper(@Nonnull SmeltingRecipe recipe) {
		return recipe;
	}

	@Override
	public boolean isRecipeValid(@Nonnull SmeltingRecipe recipe) {
		return recipe.getInputs().size() != 0 && recipe.getOutputs().size() > 0;
	}

}
