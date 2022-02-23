package mezz.jei.ingredients;

import java.util.Comparator;

public interface IIngredientSorter {

	default void doPreSort(IngredientFilter ingredientFilter, RegisteredIngredients registeredIngredients) {
	}

	Comparator<IIngredientListElementInfo<?>> getComparator(IngredientFilter ingredientFilter, RegisteredIngredients registeredIngredients);

	default void invalidateCache() {
	}
}
