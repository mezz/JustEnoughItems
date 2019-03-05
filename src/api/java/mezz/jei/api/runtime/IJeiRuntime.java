package mezz.jei.api.runtime;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.recipe.IRecipeManager;

/**
 * Gives access to JEI functions that are available once everything has loaded.
 * The IJeiRuntime instance is passed to your mod plugin in {@link IModPlugin#onRuntimeAvailable(IJeiRuntime)}.
 */
public interface IJeiRuntime {
	IRecipeManager getRecipeManager();

	IRecipesGui getRecipesGui();

	IIngredientFilter getIngredientFilter();

	IIngredientListOverlay getIngredientListOverlay();

	IBookmarkOverlay getBookmarkOverlay();

	IIngredientManager getIngredientManager();
}
