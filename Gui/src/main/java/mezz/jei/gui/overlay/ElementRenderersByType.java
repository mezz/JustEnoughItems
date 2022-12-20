package mezz.jei.gui.overlay;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.core.collect.ListMultiMap;

import java.util.Collection;
import java.util.Set;

public class ElementRenderersByType {
	private final ListMultiMap<IIngredientType<?>, ElementRenderer<?>> map = new ListMultiMap<>();

	public <T> void put(IIngredientType<T> ingredientType, ElementRenderer<T> renderer) {
		map.put(ingredientType, renderer);
	}

	public Set<IIngredientType<?>> getTypes() {
		return map.keySet();
	}

	@SuppressWarnings("unchecked")
	public <T> Collection<ElementRenderer<T>> get(IIngredientType<T> ingredientType) {
		return (Collection<ElementRenderer<T>>) (Object) map.get(ingredientType);
	}

	public void clear() {
		map.clear();
	}
}
