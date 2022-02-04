package mezz.jei.gui.ingredients;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;

import java.util.Map;
import java.util.Optional;

public class RendererOverrides {
	private final Map<IIngredientType<?>, IIngredientRenderer<?>> overrides = new Object2ObjectArrayMap<>(0);

	public <T> void addOverride(IIngredientType<T> ingredientType, IIngredientRenderer<T> ingredientRenderer) {
		this.overrides.put(ingredientType, ingredientRenderer);
	}

	public <T> Optional<IIngredientRenderer<T>> getIngredientRenderer(IIngredientType<T> ingredientType) {
		@SuppressWarnings("unchecked")
		IIngredientRenderer<T> ingredientRenderer = (IIngredientRenderer<T>) overrides.get(ingredientType);
		return Optional.ofNullable(ingredientRenderer);
	}
}
