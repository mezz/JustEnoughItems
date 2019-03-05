package mezz.jei.api.recipe.category.extensions.vanilla.crafting;

import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.category.extensions.IRecipeCategoryExtension;

/**
 * This interface allows recipes to override the default behavior in the
 * {@link VanillaRecipeCategoryUid#CRAFTING} recipe category.
 */
public interface ICustomCraftingCategoryExtension extends ICraftingCategoryExtension {
	/**
	 * This is called to override the vanilla crafting category's
	 * {@link IRecipeCategory#setRecipe(IRecipeLayout, Object, IIngredients)}
	 *
	 * Note that when this is called, the {@link IGuiItemStackGroup} has already been init with the crafting grid layout for convenience.
	 *
	 * Set the {@link IRecipeLayout} properties from this {@link IRecipeCategoryExtension} and {@link IIngredients}.
	 *
	 * @param recipeLayout the layout that needs its properties set.
	 * @param ingredients  the ingredients, already set by the recipeWrapper
	 */
	void setRecipe(IRecipeLayout recipeLayout, IIngredients ingredients);
}
