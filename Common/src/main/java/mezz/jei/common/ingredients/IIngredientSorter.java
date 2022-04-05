package mezz.jei.common.ingredients;

import java.util.Comparator;

public interface IIngredientSorter {

	default void doPreSort(IngredientFilter ingredientFilter, RegisteredIngredients registeredIngredients) {
	}

	Comparator<IListElementInfo<?>> getComparator(IngredientFilter ingredientFilter, RegisteredIngredients registeredIngredients);

	default void invalidateCache() {
	}
}
