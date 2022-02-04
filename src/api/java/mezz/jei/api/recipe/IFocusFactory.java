package mezz.jei.api.recipe;

import mezz.jei.api.ingredients.IIngredientType;

public interface IFocusFactory {
	/**
	 * Returns a new focus.
	 *
	 * @since JEI 9.3.0
	 */
	<V> IFocus<V> createFocus(RecipeIngredientRole role, IIngredientType<V> ingredientType, V ingredient);

	/**
	 * Returns a new focus.
	 *
	 * @deprecated Use {@link #createFocus(RecipeIngredientRole, IIngredientType, Object)} instead.
	 */
	@SuppressWarnings("removal")
	@Deprecated(forRemoval = true, since = "9.3.0")
	<V> IFocus<V> createFocus(IFocus.Mode mode, V ingredient);
}
