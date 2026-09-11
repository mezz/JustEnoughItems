package mezz.jei.api.runtime;

import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.UidContext;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;

/**
 * The {@link IIngredientVisibility} allows mod plugins to do advanced filtering of
 * ingredients based on what is visible in JEI.
 *
 * An instance available from {@link IJeiHelpers#getIngredientVisibility()}.
 *
 * @since JEI 9.3.0
 */
@ApiStatus.NonExtendable
public interface IIngredientVisibility {
	/**
	 * Hide registered ingredients from one or more places in JEI at runtime,
	 * without removing them from the ingredient manager.
	 *
	 * {@link UidContext#Ingredient} hides them from the ingredient list.
	 * {@link UidContext#Recipe} hides them from recipe slots and recipe catalysts.
	 * Ingredients are matched using the unique ID for each context.
	 *
	 * @param contexts the contexts where the ingredients will be hidden
	 * @since 27.34.0
	 */
	<V> void hideIngredients(
		IIngredientType<V> ingredientType,
		Collection<V> ingredients,
		Collection<UidContext> contexts
	);

	/**
	 * Unhide ingredients from one or more places in JEI at runtime.
	 * Ingredients hidden by another source, such as tags, the player's configuration,
	 * or {@link IIngredientManager#removeIngredientsAtRuntime}, will remain hidden.
	 *
	 * {@link UidContext#Ingredient} unhides them from the ingredient list.
	 * {@link UidContext#Recipe} unhides them from recipe slots and recipe catalysts.
	 * Ingredients are matched using the unique ID for each context.
	 *
	 * @param contexts the contexts where the ingredients will be unhidden
	 * @since 27.34.0
	 */
	<V> void unhideIngredients(
		IIngredientType<V> ingredientType,
		Collection<V> ingredients,
		Collection<UidContext> contexts
	);

	/**
	 * Returns true if the given ingredient is visible in JEI's ingredient list.
	 *
	 * Returns false if the given ingredient is invalid, removed by the server,
	 * hidden by a mod, or hidden by the player.
	 *
	 * @since 9.3.0
	 */
	<V> boolean isIngredientVisible(IIngredientType<V> ingredientType, V ingredient);

	/**
	 * Returns true if the given ingredient is visible in the specified UID context.
	 * {@link UidContext#Ingredient} checks the ingredient list.
	 * {@link UidContext#Recipe} checks recipe slots and recipe catalysts.
	 *
	 * Returns false if the given ingredient is invalid, removed by the server,
	 * hidden by a mod, or hidden by the player.
	 *
	 * @since 27.34.0
	 */
	<V> boolean isIngredientVisible(
		IIngredientType<V> ingredientType,
		V ingredient,
		UidContext context
	);

	/**
	 * Returns true if the given ingredient is visible in JEI's ingredient list.
	 *
	 * Returns false if the given ingredient is invalid, removed by the server,
	 * hidden by a mod, or hidden by the player.
	 *
	 * @since 10.0.0
	 */
	<V> boolean isIngredientVisible(ITypedIngredient<V> typedIngredient);

	/**
	 * Returns true if the given ingredient is visible in the specified UID context.
	 * {@link UidContext#Ingredient} checks the ingredient list.
	 * {@link UidContext#Recipe} checks recipe slots and recipe catalysts.
	 *
	 * Returns false if the given ingredient is invalid, removed by the server,
	 * hidden by a mod, or hidden by the player.
	 *
	 * @since 27.34.0
	 */
	<V> boolean isIngredientVisible(
		ITypedIngredient<V> typedIngredient,
		UidContext context
	);

	/**
	 * Register a listener that receives updates when ingredient visibility changes.
	 *
	 * @since 11.5.0
	 */
	void registerListener(IListener listener);

	/**
	 * A listener that receives updates when ingredients are made visible or invisible.
	 *
	 * @since 11.5.0
	 */
	interface IListener {
		/**
		 * Called when ingredients are made visible or invisible.
		 * @since 11.5.0
		 */
		<V> void onIngredientVisibilityChanged(ITypedIngredient<V> ingredient, boolean visible);

		/**
		 * Called when multiple ingredients are made visible or invisible.
		 * @since 27.7.0
		 */
		default <V> void onIngredientsVisibilityChanged(Collection<ITypedIngredient<V>> ingredients, boolean visible) {
			for (ITypedIngredient<V> ingredient : ingredients) {
				onIngredientVisibilityChanged(ingredient, visible);
			}
		}

		/**
		 * Called when multiple ingredients are made visible or invisible in specific contexts.
		 *
		 * @since 27.34.0
		 */
		default <V> void onIngredientsVisibilityChanged(
			Collection<ITypedIngredient<V>> ingredients,
			Collection<UidContext> contexts,
			boolean visible
		) {
			if (contexts.contains(UidContext.Ingredient)) {
				onIngredientsVisibilityChanged(ingredients, visible);
			}
		}
	}
}
