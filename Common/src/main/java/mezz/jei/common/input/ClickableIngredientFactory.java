package mezz.jei.common.input;

import mezz.jei.api.gui.builder.IClickableIngredientFactory;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IClickableIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.util.ImmutableRect2i;
import net.minecraft.client.renderer.Rect2i;

import java.util.Optional;

public class ClickableIngredientFactory implements IClickableIngredientFactory {
	private final IIngredientManager ingredientManager;

	public ClickableIngredientFactory(IIngredientManager ingredientManager) {
		this.ingredientManager = ingredientManager;
	}

	@Override
	public <T> IBuilder<T> createBuilder(ITypedIngredient<T> value) {
		return new WithIngredient<>(value);
	}

	@Override
	public <T>  IBuilder<T> createBuilder(IIngredientType<T> ingredientType, T ingredient) {
		return ingredientManager.createTypedIngredient(ingredientType, ingredient, false)
			.<IBuilder<T>>map(WithIngredient::new)
			.orElse(WithoutIngredient.getInstance());
	}

	private static class WithIngredient<T> implements IBuilder<T> {
		private final ITypedIngredient<T> ingredient;

		private WithIngredient(ITypedIngredient<T> ingredient) {
			this.ingredient = ingredient;
		}

		@Override
		public Optional<IClickableIngredient<T>> buildWithArea(int x, int y, int width, int height) {
			ImmutableRect2i area = new ImmutableRect2i(x, y, width, height);
			ClickableIngredient<T> result = new ClickableIngredient<>(ingredient, area);
			return Optional.of(result);
		}

		@Override
		public Optional<IClickableIngredient<T>> buildWithArea(Rect2i area) {
			ImmutableRect2i immutableArea = new ImmutableRect2i(area);
			ClickableIngredient<T> result = new ClickableIngredient<>(ingredient, immutableArea);
			return Optional.of(result);
		}
	}

	private static class WithoutIngredient<T> implements IBuilder<T> {
		public static final WithoutIngredient<?> INSTANCE = new WithoutIngredient<>();

		public static <T> IBuilder<T> getInstance() {
			@SuppressWarnings("unchecked")
			IBuilder<T> cast = (IBuilder<T>) INSTANCE;
			return cast;
		}

		private WithoutIngredient() {}

		@Override
		public Optional<IClickableIngredient<T>> buildWithArea(int x, int y, int width, int height) {
			return Optional.empty();
		}

		@Override
		public Optional<IClickableIngredient<T>> buildWithArea(Rect2i area) {
			return Optional.empty();
		}
	}
}
