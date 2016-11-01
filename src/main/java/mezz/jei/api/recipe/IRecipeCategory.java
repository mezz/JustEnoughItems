package mezz.jei.api.recipe;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IDrawableAnimated;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.client.Minecraft;

/**
 * Defines a category of recipe, (i.e. Crafting Table Recipe, Furnace Recipe).
 * Handles setting up the GUI for its recipe category in {@link #setRecipe(IRecipeLayout, IRecipeWrapper)}.
 * Also draws elements that are common to all recipes in the category like the background.
 *
 * @see BlankRecipeCategory
 */
public interface IRecipeCategory<T extends IRecipeWrapper> {

	/**
	 * Returns a unique ID for this recipe category.
	 * Referenced from recipes to identify which recipe category they belong to.
	 *
	 * @see IRecipeHandler#getRecipeCategoryUid(Object)
	 * @see VanillaRecipeCategoryUid
	 */
	String getUid();

	/**
	 * Returns the localized name for this recipe type.
	 * Drawn at the top of the recipe GUI pages for this category.
	 */
	String getTitle();

	/**
	 * Returns the drawable background for a single recipe in this category.
	 */
	IDrawable getBackground();

	/**
	 * Draw any extra elements that might be necessary, icons or extra slots.
	 *
	 * @see IDrawable for a simple class for drawing things.
	 * @see IGuiHelper for useful functions.
	 */
	void drawExtras(Minecraft minecraft);

	/**
	 * Draw any animations like progress bars or flashy effects.
	 * Essentially the same as {@link #drawExtras(Minecraft)} but these can be disabled in the config.
	 *
	 * @see IDrawableAnimated for a simple class for drawing animated things.
	 * @see IGuiHelper for useful functions.
	 * @deprecated since 3.13.1. Move animations into {@link #drawExtras(Minecraft)},
	 * these are being combined because nobody uses the config option to disable animations.
	 */
	@Deprecated
	void drawAnimations(Minecraft minecraft);

	/**
	 * Set the {@link IRecipeLayout} properties from the {@link IRecipeWrapper}.
	 *
	 * @deprecated since JEI 3.11.0. use {@link #setRecipe(IRecipeLayout, IRecipeWrapper, IIngredients)}
	 */
	@Deprecated
	void setRecipe(IRecipeLayout recipeLayout, T recipeWrapper);

	/**
	 * Set the {@link IRecipeLayout} properties from the {@link IRecipeWrapper} and {@link IIngredients}.
	 *
	 * @param recipeLayout  the layout that needs its properties set.
	 * @param recipeWrapper the recipeWrapper, for extra information.
	 * @param ingredients   the ingredients, already set by the recipeWrapper
	 * @since JEI 3.11.0
	 */
	void setRecipe(IRecipeLayout recipeLayout, T recipeWrapper, IIngredients ingredients);
}
