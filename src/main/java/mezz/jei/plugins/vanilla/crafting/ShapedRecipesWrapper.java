package mezz.jei.plugins.vanilla.crafting;

import net.minecraftforge.common.crafting.IShapedRecipe;

import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.recipe.wrapper.IShapedCraftingRecipeWrapper;

public class ShapedRecipesWrapper extends ShapelessRecipeWrapper<IShapedRecipe> implements IShapedCraftingRecipeWrapper {
	public ShapedRecipesWrapper(IJeiHelpers jeiHelpers, IShapedRecipe recipe) {
		super(jeiHelpers, recipe);
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
