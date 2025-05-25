package mezz.jei.common.input;

import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IClickableIngredient;
import mezz.jei.common.util.ErrorUtil;
import mezz.jei.common.util.ImmutableRect2i;
import net.minecraft.client.renderer.Rect2i;

public class ClickableIngredient<V> implements IClickableIngredient<V> {
	private final ITypedIngredient<V> value;
	private final ImmutableRect2i area;

	public ClickableIngredient(ITypedIngredient<V> value, ImmutableRect2i area) {
		ErrorUtil.checkNotNull(value, "value");
		this.value = value;
		this.area = area;
	}

	@Override
	public ITypedIngredient<V> getTypedIngredient() {
		return value;
	}

	@Override
	public Rect2i getArea() {
		return area.toMutable();
	}
}
