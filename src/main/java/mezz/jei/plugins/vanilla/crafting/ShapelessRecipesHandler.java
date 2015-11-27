package mezz.jei.plugins.vanilla.crafting;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapelessRecipes;

import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;

public class ShapelessRecipesHandler implements IRecipeHandler<ShapelessRecipes> {

	@Override
	@Nonnull
	public Class<ShapelessRecipes> getRecipeClass() {
		return ShapelessRecipes.class;
	}

	@Override
	@Nonnull
	public Class<? extends IRecipeCategory> getRecipeCategoryClass() {
		return CraftingRecipeCategory.class;
	}

	@Override
	@Nonnull
	public IRecipeWrapper getRecipeWrapper(@Nonnull ShapelessRecipes recipe) {
		return new ShapelessRecipesWrapper(recipe);
	}

	@Override
	public boolean isRecipeValid(@Nonnull ShapelessRecipes recipe) {
		if (recipe.getRecipeOutput() == null) {
			return false;
		}
		int inputCount = 0;
		for (Object input : recipe.recipeItems) {
			if (input instanceof ItemStack) {
				inputCount++;
			} else {
				return false;
			}
		}
		return inputCount > 0;
	}
}
