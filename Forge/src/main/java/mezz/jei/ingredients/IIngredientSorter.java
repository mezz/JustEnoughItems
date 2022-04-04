package mezz.jei.ingredients;

import mezz.jei.common.ingredients.RegisteredIngredients;

import java.util.Comparator;

public interface IIngredientSorter {

	default void doPreSort(IngredientFilter ingredientFilter, RegisteredIngredients registeredIngredients) {
	}

	Comparator<IListElementInfo<?>> getComparator(IngredientFilter ingredientFilter, RegisteredIngredients registeredIngredients);

	default void invalidateCache() {
	}
}
