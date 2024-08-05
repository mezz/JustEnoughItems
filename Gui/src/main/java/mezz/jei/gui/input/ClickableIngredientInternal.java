package mezz.jei.gui.input;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.util.ErrorUtil;
import mezz.jei.gui.overlay.elements.IElement;
import net.minecraft.world.item.ItemStack;

public class ClickableIngredientInternal<V> implements IClickableIngredientInternal<V> {
	private final IElement<V> element;
	private final IMouseOverable mouseOverable;
	private final boolean canClickToFocus;
	private final boolean allowsCheating;

	public ClickableIngredientInternal(IElement<V> element, IMouseOverable mouseOverable, boolean allowsCheating, boolean canClickToFocus) {
		ErrorUtil.checkNotNull(element, "element");
		this.element = element;
		this.mouseOverable = mouseOverable;
		this.allowsCheating = allowsCheating;
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
		return mouseOverable.isMouseOver(mouseX, mouseY);
	}

	@Override
	public boolean canClickToFocus() {
		return this.canClickToFocus;
	}

	@Override
	public ItemStack getCheatItemStack(IIngredientManager ingredientManager) {
		if (allowsCheating) {
			ITypedIngredient<V> value = element.getTypedIngredient();
			IIngredientHelper<V> ingredientHelper = ingredientManager.getIngredientHelper(value.getType());
			return ingredientHelper.getCheatItemStack(value.getIngredient());
		}
		return ItemStack.EMPTY;
	}
}
