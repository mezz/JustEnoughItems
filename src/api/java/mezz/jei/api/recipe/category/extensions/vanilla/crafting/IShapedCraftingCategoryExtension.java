package mezz.jei.api.recipe.category.extensions.vanilla.crafting;

import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.recipe.category.extensions.IRecipeCategoryExtension;

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
