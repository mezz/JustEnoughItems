package mezz.jei.api.recipe;

import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.ingredients.IIngredientType;

/**
 * {@link IFocusFactory} helps with creating {@link IFocus} for JEI.
 * Get an instance from {@link IJeiHelpers#getFocusFactory()}.
 */
public interface IFocusFactory {
	/**
	 * Returns a new focus.
	 *
	 * @since 9.3.0
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
