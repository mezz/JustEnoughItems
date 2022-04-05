package mezz.jei.common.input;

import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.common.util.ErrorUtil;
import mezz.jei.common.util.ImmutableRect2i;
import org.jetbrains.annotations.Nullable;

public class ClickedIngredient<V> implements IClickedIngredient<V> {
	private final ITypedIngredient<V> value;
	@Nullable
	private final ImmutableRect2i area;
	private final boolean canOverrideVanillaClickHandler;
	private final boolean allowsCheating;

	public ClickedIngredient(ITypedIngredient<V> value, @Nullable ImmutableRect2i area, boolean allowsCheating, boolean canOverrideVanillaClickHandler) {
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

	@Nullable
	@Override
	public ImmutableRect2i getArea() {
		return area;
	}

	@Override
	public boolean canOverrideVanillaClickHandler() {
		return this.canOverrideVanillaClickHandler;
	}

	@Override
	public boolean allowsCheating() {
		return allowsCheating;
	}
}
