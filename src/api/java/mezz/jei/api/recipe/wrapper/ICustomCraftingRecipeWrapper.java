package mezz.jei.api.recipe.wrapper;

import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;

/**
 * This interface allows recipes to override the default behavior in the
 * {@link VanillaRecipeCategoryUid#CRAFTING} recipe category.
 *
 * @since JEI 3.13.5
 */
public interface ICustomCraftingRecipeWrapper extends ICraftingRecipeWrapper {
	/**
	 * This is called to override the vanilla crafting category's
	 * {@link IRecipeCategory#setRecipe(IRecipeLayout, IRecipeWrapper, IIngredients)}
	 *
	 * Note that when this is called, the {@link IGuiItemStackGroup} has already been init with the crafting grid layout for convenience.
	 *
	 * Set the {@link IRecipeLayout} properties from this {@link IRecipeWrapper} and {@link IIngredients}.
	 *
	 * @param recipeLayout the layout that needs its properties set.
	 * @param ingredients  the ingredients, already set by the recipeWrapper
	 * @since JEI 3.13.5
	 */
	void setRecipe(IRecipeLayout recipeLayout, IIngredients ingredients);
}
