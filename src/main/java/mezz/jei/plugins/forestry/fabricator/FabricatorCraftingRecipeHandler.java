package mezz.jei.plugins.forestry.fabricator;

import javax.annotation.Nonnull;

import forestry.factory.gadgets.MachineFabricator;

import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;

public class FabricatorCraftingRecipeHandler implements IRecipeHandler {

	@Override
	public Class getRecipeClass() {
		return MachineFabricator.Recipe.class;
	}

	@Override
	public Class<? extends IRecipeCategory> getRecipeCategoryClass() {
		return FabricatorRecipeCategory.class;
	}

	@Override
	public IRecipeWrapper getRecipeWrapper(@Nonnull Object recipe) {
		return new FabricatorCraftingRecipeWrapper(recipe);
	}

}
