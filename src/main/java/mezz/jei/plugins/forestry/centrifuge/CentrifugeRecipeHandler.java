package mezz.jei.plugins.forestry.centrifuge;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import forestry.factory.gadgets.MachineCentrifuge;

import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;

public class CentrifugeRecipeHandler implements IRecipeHandler {

	@Nullable
	@Override
	public Class getRecipeClass() {
		return MachineCentrifuge.Recipe.class;
	}

	@Nonnull
	@Override
	public Class<? extends IRecipeCategory> getRecipeCategoryClass() {
		return CentrifugeRecipeCategory.class;
	}

	@Override
	public IRecipeWrapper getRecipeWrapper(@Nonnull Object recipe) {
		return new CentrifugeRecipeWrapper(recipe);
	}

}
