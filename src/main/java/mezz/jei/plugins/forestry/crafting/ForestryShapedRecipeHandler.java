package mezz.jei.plugins.forestry.crafting;

import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.plugins.vanilla.crafting.CraftingRecipeCategory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ForestryShapedRecipeHandler implements IRecipeHandler {

	@Nullable
	@Override
	public Class getRecipeClass() {
		try {
			return Class.forName("forestry.core.utils.ShapedRecipeCustom");
		} catch (ClassNotFoundException e) {
			return null;
		}
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
