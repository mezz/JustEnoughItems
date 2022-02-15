package mezz.jei.api.recipe;

import mezz.jei.api.ingredients.IIngredientType;

import java.util.List;
import java.util.stream.Stream;

/**
 * Represents multiple {@link IFocus} used for recipe lookups.
 *
 * Helper functions make this simpler to use than a regular `List<IFocus<?>>`
 * when you only care about certain types of focuses.
 *
 * @since 9.4.0
 */
public interface IFocusGroup {
	/**
	 * When the player is looking at all recipes in a category,
	 * there will be no focused ingredient and this group will be empty.
	 *
	 * @since 9.4.0
	 */
	boolean isEmpty();

	/**
	 * Get a raw list of all the current focuses.
	 *
	 * @since 9.4.0
	 */
	List<IFocus<?>> getAllFocuses();

	/**
	 * Get a stream of the current focuses filtered by role.
	 *
	 * @since 9.4.0
	 */
	Stream<IFocus<?>> getFocuses(RecipeIngredientRole role);

	/**
	 * Get a stream of the current focuses filtered by type.
	 *
	 * @since 9.4.0
	 */
	<T> Stream<IFocus<T>> getFocuses(IIngredientType<T> ingredientType);

	/**
	 * Get a stream of the current focuses filtered by type and role.
	 *
	 * @since 9.4.0
	 */
	<T> Stream<IFocus<T>> getFocuses(IIngredientType<T> ingredientType, RecipeIngredientRole role);
}
