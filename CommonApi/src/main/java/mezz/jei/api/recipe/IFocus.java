package mezz.jei.api.recipe;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;

import java.util.Optional;

/**
 * The current search focus.
 * Set by the player when they look up the recipe. The ingredient being looked up is the focus.
 * This class is immutable, the value and mode do not change.
 *
 * Create a focus with {@link IFocusFactory#createFocus(RecipeIngredientRole, IIngredientType, Object)}.
 *
 * Use a null IFocus to signify no focus, like in the case of looking up categories of recipes.
 */
public interface IFocus<V> {
	/**
	 * The ingredient that is being focused on.
	 *
	 * @since 9.3.0
	 */
	ITypedIngredient<V> getTypedValue();

	/**
	 * The focused recipe ingredient role.
	 *
	 * @since 9.3.0
	 */
	RecipeIngredientRole getRole();

	/**
	 * @return this focus if it matches the given ingredient type.
	 * This is useful when handling a wildcard generic instance of `IFocus<?>`.
	 *
	 * @since 9.4.0
	 */
	<T> Optional<IFocus<T>> checkedCast(IIngredientType<T> ingredientType);
}
