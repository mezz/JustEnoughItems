package mezz.jei.api.ingredients;

/**
 * Ingredient with type information, for type safety.
 * These ingredients are validated by JEI, and only contain valid types and ingredients.
 *
 * @since 9.3.0
 */
public interface ITypedIngredient<T> {
	/**
	 * @return the type of this ingredient
	 * @see IIngredientType
	 *
	 * @since 9.3.0
	 */
	IIngredientType<T> getType();

	/**
	 * @return the ingredient wrapped by this instance
	 *
	 * @since 9.3.0
	 */
	T getIngredient();
}
