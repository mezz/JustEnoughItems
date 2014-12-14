package mezz.jei.plugins.forestry.crafting;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import forestry.core.utils.ShapedRecipeCustom;

import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.plugins.vanilla.crafting.CraftingRecipeCategory;

public class ForestryShapedRecipeHandler implements IRecipeHandler {

	@Nullable
	@Override
	public Class getRecipeClass() {
		return ShapedRecipeCustom.class;
	}

	@Nonnull
	@Override
	public Class<? extends IRecipeCategory> getRecipeCategoryClass() {
		return CraftingRecipeCategory.class;
	}

	@Override
	public IRecipeWrapper getRecipeWrapper(@Nonnull Object recipe) {
		return new ForestryShapedRecipeWrapper(recipe);
	}
}
