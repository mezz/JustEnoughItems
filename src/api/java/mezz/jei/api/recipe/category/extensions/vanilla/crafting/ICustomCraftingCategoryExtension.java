package mezz.jei.api.recipe.category.extensions.vanilla.crafting;

import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.category.extensions.IRecipeCategoryExtension;

import java.util.List;

/**
 * This interface allows recipes to override the default behavior in the
 * {@link VanillaRecipeCategoryUid#CRAFTING} recipe category.
 *
 * @deprecated Use {@link ICraftingCategoryExtension#setRecipe(IRecipeLayoutBuilder, ICraftingGridHelper, List)} instead.
 */
@Deprecated(forRemoval = true, since = "9.3.0")
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
	 *
	 * @deprecated Use {@link ICraftingCategoryExtension#setRecipe(IRecipeLayoutBuilder, ICraftingGridHelper, List)} instead.
	 */
	@Deprecated(forRemoval = true, since = "9.3.0")
	void setRecipe(IRecipeLayout recipeLayout, IIngredients ingredients);
}
