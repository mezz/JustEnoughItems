package mezz.jei.library.plugins.debug.ingredients;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class ErrorIngredientListFactory {
	private ErrorIngredientListFactory() {
	}

	public static Collection<ErrorIngredient> create() {
		List<ErrorIngredient> ingredients = new ArrayList<>();
		for (ErrorIngredient.CrashType crashType : ErrorIngredient.CrashType.values()) {
			ErrorIngredient ingredient = new ErrorIngredient(crashType);
			ingredients.add(ingredient);
		}
		return ingredients;
	}
}
