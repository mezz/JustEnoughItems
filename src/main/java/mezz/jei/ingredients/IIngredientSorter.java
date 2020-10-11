package mezz.jei.ingredients;

import java.util.Comparator;
import java.util.Set;

public interface IIngredientSorter {
	Comparator<IIngredientListElementInfo<?>> getComparator(Set<String> allValues);

	default void invalidateCache() {}
}
