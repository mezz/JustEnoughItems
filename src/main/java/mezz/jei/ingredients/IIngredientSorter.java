package mezz.jei.ingredients;

import mezz.jei.api.runtime.IIngredientManager;

import java.util.Comparator;

public interface IIngredientSorter {

	default void doPreSort(IngredientFilter ingredientFilter, IIngredientManager ingredientManager) {
	}

	Comparator<IIngredientListElementInfo<?>> getComparator(IngredientFilter ingredientFilter, IIngredientManager ingredientManager);

	default void invalidateCache() {
	}
}
