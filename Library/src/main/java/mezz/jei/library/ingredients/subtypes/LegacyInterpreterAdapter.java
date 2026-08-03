package mezz.jei.library.ingredients.subtypes;

import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;

public class LegacyInterpreterAdapter<T> implements ISubtypeInterpreter<T> {
	private final IIngredientSubtypeInterpreter<T> legacyInterpreter;

	public LegacyInterpreterAdapter(IIngredientSubtypeInterpreter<T> legacyInterpreter) {
		this.legacyInterpreter = legacyInterpreter;
	}

	@Override
	public Object getSubtypeData(T ingredient, UidContext context) {
		String result = legacyInterpreter.apply(ingredient, context);
		if (result.isEmpty()) {
			return null;
		}
		return result;
	}

	@Override
	public String getLegacyStringSubtypeInfo(T ingredient, UidContext context) {
		return legacyInterpreter.apply(ingredient, context);
	}
}
