package mezz.jei.common.render;

import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.util.IImmutableRect2i;
import mezz.jei.api.runtime.IClickedIngredient;
import mezz.jei.common.util.ImmutableRect2i;

import java.util.Optional;

public class ElementRenderer<T> implements IClickedIngredient<T> {
	private static final ImmutableRect2i DEFAULT_AREA = new ImmutableRect2i(0, 0, 16, 16);

	private final ITypedIngredient<T> ingredient;
	private ImmutableRect2i area = DEFAULT_AREA;
	private int padding;

	public ElementRenderer(ITypedIngredient<T> ingredient) {
		this.ingredient = ingredient;
	}

	public void setArea(ImmutableRect2i area) {
		this.area = area;
	}

	public void setPadding(int padding) {
		this.padding = padding;
	}

	@Override
	public ITypedIngredient<T> getTypedIngredient() {
		return ingredient;
	}

	@Override
	public Optional<IImmutableRect2i> getArea() {
		return Optional.of(area);
	}

	public IImmutableRect2i getElementArea() {
		return area;
	}

	@Override
	public boolean allowsCheating() {
		return true;
	}

	@Override
	public boolean canOverrideVanillaClickHandler() {
		return true;
	}

	public int getPadding() {
		return padding;
	}
}
