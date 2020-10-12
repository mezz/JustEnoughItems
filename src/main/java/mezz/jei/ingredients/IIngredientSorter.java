package mezz.jei.ingredients;

import mezz.jei.api.ingredients.IIngredientType;

import java.util.Collection;
import java.util.Comparator;

public interface IIngredientSorter {
	Comparator<IIngredientListElementInfo<?>> getComparator(Collection<String> modNames, Collection<IIngredientType<?>> ingredientTypes);

	default void invalidateCache() {}
}
