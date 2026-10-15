package mezz.jei.gui.input;

import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.common.input.IMouseOverable;
import mezz.jei.common.util.ErrorUtil;
import mezz.jei.gui.overlay.elements.IElement;

public class ClickableIngredientInternal<V> implements IClickableIngredientInternal<V> {
	private final IElement<V> element;
	private final IMouseOverable mouseOverable;
	private final boolean canClickToFocus;
	private final boolean canClickToCheat;

	public ClickableIngredientInternal(IElement<V> element, IMouseOverable mouseOverable, boolean canClickToCheat, boolean canClickToFocus) {
		ErrorUtil.checkNotNull(element, "element");
		this.element = element;
		this.mouseOverable = mouseOverable;
		this.canClickToCheat = canClickToCheat;
		this.canClickToFocus = canClickToFocus;
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
	public boolean isMouseOver(double mouseX, double mouseY) {
		return mouseOverable.containsMouse(mouseX, mouseY);
	}

	@Override
	public boolean canClickToFocus() {
		return this.canClickToFocus;
	}

	@Override
	public boolean canClickToCheat() {
		return this.canClickToCheat;
	}
}
