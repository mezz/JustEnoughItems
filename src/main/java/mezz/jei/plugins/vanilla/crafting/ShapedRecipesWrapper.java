package mezz.jei.plugins.vanilla.crafting;

import net.minecraft.item.crafting.ShapedRecipes;

import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.recipe.wrapper.IShapedCraftingRecipeWrapper;

public class ShapedRecipesWrapper extends ShapelessRecipeWrapper<ShapedRecipes> implements IShapedCraftingRecipeWrapper {
	public ShapedRecipesWrapper(IJeiHelpers jeiHelpers, ShapedRecipes recipe) {
		super(jeiHelpers, recipe);
	}

	@Override
	public int getWidth() {
		return recipe.recipeWidth;
	}

	@Override
	public int getHeight() {
		return recipe.recipeHeight;
	}
}
