package mezz.jei.gui.recipes;

import java.util.List;
import java.util.function.Predicate;

final class IngredientGridReplacement {
	private IngredientGridReplacement() {
	}

	public static <T> void replace(List<T> elements, Predicate<T> isIngredientGrid, T replacement) {
		int firstGridIndex = -1;
		for (int i = 0; i < elements.size(); i++) {
			T element = elements.get(i);
			if (!isIngredientGrid.test(element)) {
				continue;
			}
			if (firstGridIndex < 0) {
				firstGridIndex = i;
				elements.set(i, replacement);
			} else {
				elements.remove(i);
				i--;
			}
		}
		if (firstGridIndex < 0) {
			elements.add(replacement);
		}
	}
}
