package mezz.jei.api.runtime;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.ingredients.IIngredientType;

/**
 * The {@link IIngredientManager} is provided by JEI and has some useful functions related to recipe ingredients.
 * An instance is passed to your plugin in {@link IModPlugin#registerRecipes} and it is accessible from
 * {@link IJeiRuntime#getIngredientVisibility()}.
 *
 * @since JEI 9.3.0
 */
public interface IIngredientVisibility {
	/**
	 * Returns true if the given ingredient is visible in JEI's ingredient list.
	 *
	 * Returns false if the given ingredient is invalid, removed by the server, hidden by a mod, or hidden by the player.
	 *
	 * @since JEI 9.3.0
	 */
	<V> boolean isIngredientVisible(IIngredientType<V> ingredientType, V ingredient);
}
