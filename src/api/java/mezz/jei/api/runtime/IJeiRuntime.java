package mezz.jei.api.runtime;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeIngredientRole;

/**
 * Gives access to JEI functions that are available once everything has loaded.
 * The IJeiRuntime instance is passed to your mod plugin in {@link IModPlugin#onRuntimeAvailable(IJeiRuntime)}.
 */
public interface IJeiRuntime {
	/**
	 * The {@link IRecipeManager} offers several functions for retrieving and handling recipes.
	 */
	IRecipeManager getRecipeManager();

	/**
	 * The {@link IRecipesGui} is JEI's gui for displaying recipes.
	 * Use this interface to open the gui and display recipes.
	 */
	IRecipesGui getRecipesGui();

	/**
	 * The {@link IIngredientFilter} is JEI's filter that can be set by players or controlled by mods.
	 * Use this interface to get information from and interact with it.
	 */
	IIngredientFilter getIngredientFilter();

	/**
	 * The {@link IIngredientListOverlay} is JEI's gui that displays all the ingredients next to an open container gui.
	 * Use this interface to get information from and interact with it.
	 */
	IIngredientListOverlay getIngredientListOverlay();

	/**
	 * The {@link IBookmarkOverlay} is JEI's gui that displays all the bookmarked ingredients next to an open container gui.
	 * Use this interface to get information from it.
	 */
	IBookmarkOverlay getBookmarkOverlay();

	/**
	 * {@link IJeiHelpers} provides helpers and tools for addon mods.
	 *
	 * @since 9.4.2
	 */
	IJeiHelpers getJeiHelpers();

	/**
	 * The {@link IIngredientManager} has some useful functions related to recipe ingredients.
	 */
	IIngredientManager getIngredientManager();

	/**
	 * The {@link IIngredientVisibility} allows mod plugins to do advanced filtering of
	 * ingredients based on what is visible in JEI.
	 *
	 * @since 9.3.0
	 */
	IIngredientVisibility getIngredientVisibility();

	/**
	 * Returns a new typed ingredient.
	 *
	 * @since 9.3.0
	 */
	<T> ITypedIngredient<T> createTypedIngredient(IIngredientType<T> ingredientType, T ingredient);

	/**
	 * Returns a new focus.
	 *
	 * @since 9.3.0
	 * @deprecated Use {@link IJeiHelpers#getFocusFactory()} instead, it's available to use much earlier.
	 */
	@Deprecated(forRemoval = true, since = "9.4.0")
	<T> IFocus<T> createFocus(RecipeIngredientRole role, IIngredientType<T> ingredientType, T ingredient);
}
