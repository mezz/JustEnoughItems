package mezz.jei.ingredients;

import java.util.Comparator;

public interface IIngredientSorter {
	Comparator<IIngredientListElementInfo<?>> getComparator();
}
