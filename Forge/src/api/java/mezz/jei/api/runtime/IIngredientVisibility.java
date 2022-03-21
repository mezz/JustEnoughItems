package mezz.jei.api.runtime;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.registration.IRecipeRegistration;

/**
 * The {@link IIngredientVisibility} allows mod plugins to do advanced filtering of
 * ingredients based on what is visible in JEI.
 *
 * An instance available during {@link IModPlugin#registerRecipes}
 * from {@link IRecipeRegistration#getIngredientVisibility()}
 * and it is accessible at runtime from
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
