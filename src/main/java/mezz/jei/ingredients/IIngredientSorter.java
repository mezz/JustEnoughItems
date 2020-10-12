package mezz.jei.ingredients;

import mezz.jei.api.runtime.IIngredientManager;

import java.util.Comparator;

public interface IIngredientSorter {
	Comparator<IIngredientListElementInfo<?>> getComparator(IngredientFilter ingredientFilter, IIngredientManager ingredientManager);

	default void invalidateCache() {}
}
