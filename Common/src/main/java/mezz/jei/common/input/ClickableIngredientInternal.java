package mezz.jei.common.input;

import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.common.util.ErrorUtil;
import mezz.jei.common.util.ImmutableRect2i;

public class ClickableIngredientInternal<V> implements IClickableIngredientInternal<V> {
	private final ITypedIngredient<V> value;
	private final ImmutableRect2i area;
	private final boolean canOverrideVanillaClickHandler;
	private final boolean allowsCheating;

	public ClickableIngredientInternal(ITypedIngredient<V> value, ImmutableRect2i area, boolean allowsCheating, boolean canOverrideVanillaClickHandler) {
		ErrorUtil.checkNotNull(value, "value");
		this.value = value;
		this.area = area;
		this.allowsCheating = allowsCheating;
		this.canOverrideVanillaClickHandler = canOverrideVanillaClickHandler;
	}

	@Override
	public ITypedIngredient<V> getTypedIngredient() {
		return value;
	}

	@Override
	public ImmutableRect2i getArea() {
		return area;
	}

	@Override
	public boolean canClickToFocus() {
		return this.canOverrideVanillaClickHandler;
	}

	@Override
	public boolean allowsCheating() {
		return allowsCheating;
	}
}
