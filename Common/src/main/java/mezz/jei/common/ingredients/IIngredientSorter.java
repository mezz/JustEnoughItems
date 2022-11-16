package mezz.jei.common.ingredients;

import mezz.jei.api.ingredients.IRegisteredIngredients;

import java.util.Comparator;

public interface IIngredientSorter {

	default void doPreSort(IngredientFilter ingredientFilter, IRegisteredIngredients registeredIngredients) {
	}

	Comparator<IListElementInfo<?>> getComparator(IngredientFilter ingredientFilter, IRegisteredIngredients registeredIngredients);

	default void invalidateCache() {
	}
}
