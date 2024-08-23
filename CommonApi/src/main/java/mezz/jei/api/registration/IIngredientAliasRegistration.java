package mezz.jei.api.registration;

import mezz.jei.api.ingredients.IIngredientType;

import java.util.Collection;
import java.util.List;

/**
 * Allows registration of search aliases for ingredients.
 * Search aliases allow mods to add alternative names for ingredients, to help players find them more easily.
 *
 * @since 19.10.0
 */
public interface IIngredientAliasRegistration {
	/**
	 * Register a search alias for an ingredient.
	 *
	 * @since 19.10.0
	 */
	<I> void addAlias(IIngredientType<I> type, I ingredient, String alias);

	/**
	 * Register multiple search aliases for an ingredient.
	 *
	 * @since 19.10.0
	 */
	<I> void addAliases(IIngredientType<I> type, I ingredient, Collection<String> aliases);

	/**
	 * Register a search aliases for multiple ingredients.
	 *
	 * @since 19.10.0
	 */
	<I> void addAliases(IIngredientType<I> type, List<I> ingredients, String alias);

	/**
	 * Register multiple search aliases for multiple ingredients.
	 *
	 * @since 19.10.0
	 */
	<I> void addAliases(IIngredientType<I> type, Collection<I> ingredients, Collection<String> aliases);
}
