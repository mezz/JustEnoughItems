package mezz.jei.api.recipe.category.extensions;

import mezz.jei.api.recipe.VanillaRecipeCategoryUid;

/**
 * Implement this interface instead of just {@link IRecipeCategoryExtension} to have your extension work as part of the
 * {@link VanillaRecipeCategoryUid#CRAFTING} recipe category as a shaped recipe.
 *
 * For shapeless recipes, implement {@link ICraftingCategoryExtension} instead.
 */
public interface IShapedCraftingCategoryExtension extends ICraftingCategoryExtension {

	int getWidth();

	int getHeight();

}
