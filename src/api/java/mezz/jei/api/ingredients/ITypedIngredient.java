package mezz.jei.api.ingredients;

/**
 * Ingredient with type information, for type safety.
 */
public interface ITypedIngredient<T> {
	IIngredientType<T> getType();
	T getIngredient();
}
