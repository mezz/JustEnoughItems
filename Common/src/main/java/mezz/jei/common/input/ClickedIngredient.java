package mezz.jei.common.input;

import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IClickedIngredient;
import mezz.jei.api.runtime.util.IImmutableRect2i;
import mezz.jei.common.util.ErrorUtil;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ClickedIngredient<V> implements IClickedIngredient<V> {
	private final ITypedIngredient<V> value;
	@Nullable
	private final IImmutableRect2i area;
	private final boolean canOverrideVanillaClickHandler;
	private final boolean allowsCheating;

	public ClickedIngredient(ITypedIngredient<V> value, @Nullable IImmutableRect2i area, boolean allowsCheating, boolean canOverrideVanillaClickHandler) {
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
	public Optional<IImmutableRect2i> getArea() {
		return Optional.ofNullable(area);
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
