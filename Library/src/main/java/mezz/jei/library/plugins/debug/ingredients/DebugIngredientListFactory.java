package mezz.jei.library.plugins.debug.ingredients;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class DebugIngredientListFactory {
	private DebugIngredientListFactory() {
	}

	public static Collection<DebugIngredient> create(int start, int end) {
		List<DebugIngredient> ingredients = new ArrayList<>();
		for (int i = start; i < end; i++) {
			DebugIngredient debugIngredient = new DebugIngredient(i);
			ingredients.add(debugIngredient);
		}
		return ingredients;
	}
}
