package mezz.jei.render;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.collect.ListMultiMap;

import java.util.Collection;
import java.util.Set;

public class IngredientListBatches {
	private final ListMultiMap<IIngredientType<?>, IngredientListElementRenderer<?>> map = new ListMultiMap<>();

	public <T> void put(IIngredientType<T> ingredientType, IngredientListElementRenderer<T> renderer) {
		map.put(ingredientType, renderer);
	}

	public Set<IIngredientType<?>> getTypes() {
		return map.keySet();
	}

	@SuppressWarnings("unchecked")
	public <T> Collection<IngredientListElementRenderer<T>> get(IIngredientType<T> ingredientType) {
		return (Collection<IngredientListElementRenderer<T>>) (Object) map.get(ingredientType);
	}

	public void clear() {
		map.clear();
	}
}
