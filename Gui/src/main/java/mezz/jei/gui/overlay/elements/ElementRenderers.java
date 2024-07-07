package mezz.jei.gui.overlay.elements;

import mezz.jei.api.ingredients.IIngredientType;

import java.util.HashMap;
import java.util.Map;

public class ElementRenderers {
	private final Map<IIngredientType<?>, ElementRenderer<?>> map = new HashMap<>();

	public <T> ElementRenderer<T> get(IIngredientType<T> ingredientType) {
		@SuppressWarnings("unchecked")
		ElementRenderer<T> result = (ElementRenderer<T>) this.map.get(ingredientType);
		if (result == null) {
			result = new ElementRenderer<>(ingredientType);
			this.map.put(ingredientType, result);
		}
		return result;
	}
}
