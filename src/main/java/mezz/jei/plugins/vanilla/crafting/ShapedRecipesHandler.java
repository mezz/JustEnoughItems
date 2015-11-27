package mezz.jei.plugins.vanilla.crafting;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapedRecipes;

import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;

public class ShapedRecipesHandler implements IRecipeHandler<ShapedRecipes> {

	@Override
	@Nonnull
	public Class<ShapedRecipes> getRecipeClass() {
		return ShapedRecipes.class;
	}

	@Override
	@Nonnull
	public Class<? extends IRecipeCategory> getRecipeCategoryClass() {
		return CraftingRecipeCategory.class;
	}

	@Override
	@Nonnull
	public IRecipeWrapper getRecipeWrapper(@Nonnull ShapedRecipes recipe) {
		return new ShapedRecipesWrapper(recipe);
	}

	@Override
	public boolean isRecipeValid(@Nonnull ShapedRecipes recipe) {
		if (recipe.getRecipeOutput() == null) {
			return false;
		}
		int inputCount = 0;
		for (ItemStack input : recipe.recipeItems) {
			if (input != null) {
				inputCount++;
			}
		}
		return inputCount > 0;
	}
}
