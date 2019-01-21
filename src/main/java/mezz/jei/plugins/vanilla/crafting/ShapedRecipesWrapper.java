package mezz.jei.plugins.vanilla.crafting;

import net.minecraftforge.common.crafting.IShapedRecipe;

import mezz.jei.api.recipe.category.extensions.IShapedCraftingRecipeWrapper;

public class ShapedRecipesWrapper extends ShapelessRecipeWrapper<IShapedRecipe> implements IShapedCraftingRecipeWrapper {
	public ShapedRecipesWrapper(IShapedRecipe recipe) {
		super(recipe);
	}

	@Override
	public int getWidth() {
		return recipe.getRecipeWidth();
	}

	@Override
	public int getHeight() {
		return recipe.getRecipeHeight();
	}
}
