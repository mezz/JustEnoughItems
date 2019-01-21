package mezz.jei.api.recipe.category.extensions;

import mezz.jei.api.recipe.VanillaRecipeCategoryUid;

/**
 * Implement this interface instead of just {@link IRecipeWrapper} to have your recipe wrapper work as part of the
 * {@link VanillaRecipeCategoryUid#CRAFTING} recipe category as a shaped recipe.
 *
 * For shapeless recipes, use {@link ICraftingRecipeWrapper}.
 */
public interface IShapedCraftingRecipeWrapper extends ICraftingRecipeWrapper {

	int getWidth();

	int getHeight();

}
