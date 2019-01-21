package mezz.jei.api;

import mezz.jei.api.gui.IRecipesGui;
import mezz.jei.api.recipe.IRecipeRegistry;

/**
 * Gives access to JEI functions that are available once everything has loaded.
 * The IJeiRuntime instance is passed to your mod plugin in {@link IModPlugin#onRuntimeAvailable(IJeiRuntime)}.
 */
public interface IJeiRuntime {
	IRecipeRegistry getRecipeRegistry();

	IRecipesGui getRecipesGui();

	IIngredientFilter getIngredientFilter();

	IIngredientListOverlay getIngredientListOverlay();
}
