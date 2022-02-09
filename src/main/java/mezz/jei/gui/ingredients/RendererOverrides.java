package mezz.jei.gui.ingredients;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.runtime.IIngredientManager;

import java.util.Map;
import java.util.Optional;

public class RendererOverrides {
	private final Map<IIngredientType<?>, IIngredientRenderer<?>> overrides = new Object2ObjectArrayMap<>(0);
	private int width;
	private int height;

	public <T> void addOverride(IIngredientType<T> ingredientType, IIngredientRenderer<T> ingredientRenderer) {
		int width = ingredientRenderer.getWidth();
		int height = ingredientRenderer.getHeight();
		Preconditions.checkArgument(width > 0, "ingredient renderer width must be > 0");
		Preconditions.checkArgument(height > 0, "ingredient renderer height must be > 0");

		if (this.width == 0 && this.height == 0) {
			this.width = width;
			this.height = height;
		} else {
			Preconditions.checkArgument(
				this.width == width,
				"All ingredient render overrides for one slot must have the same width."
			);
			Preconditions.checkArgument(
				this.height == height,
				"All ingredient render overrides for one slot must have the same height."
			);
		}
		this.overrides.put(ingredientType, ingredientRenderer);
	}

	public <T> Optional<IIngredientRenderer<T>> getIngredientRenderer(IIngredientType<T> ingredientType) {
		@SuppressWarnings("unchecked")
		IIngredientRenderer<T> ingredientRenderer = (IIngredientRenderer<T>) overrides.get(ingredientType);
		return Optional.ofNullable(ingredientRenderer);
	}

	public int getIngredientWidth() {
		if (width <= 0) {
			return 16;
		}
		return width;
	}

	public int getIngredientHeight() {
		if (height <= 0) {
			return 16;
		}
		return height;
	}
}
