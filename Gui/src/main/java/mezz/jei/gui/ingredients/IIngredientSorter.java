package mezz.jei.gui.ingredients;

import mezz.jei.api.ingredients.IRegisteredIngredients;
import mezz.jei.common.ingredients.IListElementInfo;

import java.util.Comparator;

public interface IIngredientSorter {

	default void doPreSort(IngredientFilter ingredientFilter, IRegisteredIngredients registeredIngredients) {
	}

	Comparator<IListElementInfo<?>> getComparator(IngredientFilter ingredientFilter, IRegisteredIngredients registeredIngredients);

	default void invalidateCache() {
	}
}
