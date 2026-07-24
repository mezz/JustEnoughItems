package mezz.jei.gui.input;

import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.common.util.ErrorUtil;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.gui.overlay.elements.IElement;

public class DraggableIngredientInternal<V> implements IDraggableIngredientInternal<V> {
	private final IElement<V> element;
	private final ImmutableRect2i area;

	public DraggableIngredientInternal(IElement<V> element, ImmutableRect2i area) {
		ErrorUtil.checkNotNull(element, "element");
		ErrorUtil.checkNotNull(area, "area");
		this.element = element;
		this.area = area;
	}

	@Override
	public ITypedIngredient<V> getTypedIngredient() {
		return element.getTypedIngredient();
	}

	@Override
	public IElement<V> getElement() {
		return element;
	}

	@Override
	public ImmutableRect2i getArea() {
		return area;
	}
}
