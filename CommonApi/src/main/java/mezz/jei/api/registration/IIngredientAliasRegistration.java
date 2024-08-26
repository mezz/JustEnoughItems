package mezz.jei.api.registration;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;

import java.util.Collection;

/**
 * Allows registration of search aliases for ingredients.
 * Search aliases allow mods to add alternative names for ingredients, to help players find them more easily.
 *
 * @since 15.15.0
 */
public interface IIngredientAliasRegistration {
	/**
	 * Register a search alias for an ingredient.
	 * An alias may be a translation key.
	 *
	 * @since 15.15.0
	 */
	<I> void addAlias(IIngredientType<I> type, I ingredient, String alias);

	/**
	 * Register a search alias for an ingredient.
	 * An alias may be a translation key.
	 *
	 * @since 15.15.0
	 */
	<I> void addAlias(ITypedIngredient<I> typedIngredient, String alias);

	/**
	 * Register multiple search aliases for an ingredient.
	 * An alias may be a translation key.
	 *
	 * @since 15.15.0
	 */
	<I> void addAliases(IIngredientType<I> type, I ingredient, Collection<String> aliases);

	/**
	 * Register multiple search aliases for an ingredient.
	 * An alias may be a translation key.
	 *
	 * @since 15.15.0
	 */
	<I> void addAliases(ITypedIngredient<I> typedIngredient, Collection<String> aliases);

	/**
	 * Register a search aliases for multiple ingredients.
	 * An alias may be a translation key.
	 *
	 * @since 15.15.0
	 */
	<I> void addAliases(IIngredientType<I> type, Collection<I> ingredients, String alias);

	/**
	 * Register a search aliases for multiple ingredients.
	 * An alias may be a translation key.
	 *
	 * @since 15.15.0
	 */
	<I> void addAliases(Collection<ITypedIngredient<I>> typedIngredients, String alias);

	/**
	 * Register multiple search aliases for multiple ingredients.
	 * An alias may be a translation key.
	 *
	 * @since 15.15.0
	 */
	<I> void addAliases(IIngredientType<I> type, Collection<I> ingredients, Collection<String> aliases);

	/**
	 * Register multiple search aliases for multiple ingredients.
	 * An alias may be a translation key.
	 *
	 * @since 15.15.0
	 */
	<I> void addAliases(Collection<ITypedIngredient<I>> typedIngredients, Collection<String> aliases);
}
