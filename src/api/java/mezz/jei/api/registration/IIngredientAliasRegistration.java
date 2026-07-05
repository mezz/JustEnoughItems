package mezz.jei.api.registration;

import java.util.Collection;

import mezz.jei.api.recipe.IIngredientType;

/**
 * Allows registration of search aliases for ingredients.
 * Search aliases allow mods to add alternative names for ingredients, to help players find them more easily.
 *
 * @since JEI 4.20.0
 */
public interface IIngredientAliasRegistration {
	/**
	 * Register one search alias for all subtypes of an ingredient.
	 * The ingredient is used to identify the group through
	 * {@link mezz.jei.api.ingredients.IIngredientHelper#getWildcardId(Object)}.
	 * An alias may be a translation key.
	 *
	 * @since JEI 4.22.0
	 */
	<I> void addAliasToAllSubtypes(IIngredientType<I> type, I ingredient, String alias);

	/**
	 * Register multiple search aliases for all subtypes of an ingredient.
	 * The ingredient is used to identify the group through
	 * {@link mezz.jei.api.ingredients.IIngredientHelper#getWildcardId(Object)}.
	 * An alias may be a translation key.
	 *
	 * @since JEI 4.22.0
	 */
	<I> void addAliasesToAllSubtypes(IIngredientType<I> type, I ingredient, Collection<String> aliases);

	/**
	 * Register one search alias for an ingredient. An alias may be a translation key.
	 *
	 * @since JEI 4.20.0
	 */
	<I> void addAlias(IIngredientType<I> type, I ingredient, String alias);

	/**
	 * Register multiple search aliases for an ingredient. An alias may be a translation key.
	 *
	 * @since JEI 4.20.0
	 */
	<I> void addAliases(IIngredientType<I> type, I ingredient, Collection<String> aliases);

	/**
	 * Register one search alias for multiple ingredients. An alias may be a translation key.
	 *
	 * @since JEI 4.20.0
	 */
	<I> void addAliases(IIngredientType<I> type, Collection<I> ingredients, String alias);

	/**
	 * Register multiple search aliases for multiple ingredients. An alias may be a translation key.
	 *
	 * @since JEI 4.20.0
	 */
	<I> void addAliases(IIngredientType<I> type, Collection<I> ingredients, Collection<String> aliases);
}
