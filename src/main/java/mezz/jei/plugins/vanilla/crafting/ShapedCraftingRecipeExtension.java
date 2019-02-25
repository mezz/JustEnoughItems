package mezz.jei.plugins.vanilla.crafting;

import net.minecraftforge.common.crafting.IShapedRecipe;

import mezz.jei.api.recipe.category.extensions.IShapedCraftingCategoryExtension;

public class ShapedCraftingRecipeExtension extends ShapelessCraftingCategoryExtension<IShapedRecipe> implements IShapedCraftingCategoryExtension {
	public ShapedCraftingRecipeExtension(IShapedRecipe recipe) {
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
