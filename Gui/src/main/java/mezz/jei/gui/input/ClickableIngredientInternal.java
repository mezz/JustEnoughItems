package mezz.jei.gui.input;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.util.ErrorUtil;
import mezz.jei.gui.overlay.elements.IElement;
import net.minecraft.world.item.ItemStack;

public class ClickableIngredientInternal implements IClickableIngredientInternal {
	private final IElement element;
	private final IMouseOverable mouseOverable;
	private final boolean canClickToFocus;
	private final boolean allowsCheating;

	public ClickableIngredientInternal(IElement element, IMouseOverable mouseOverable, boolean allowsCheating, boolean canClickToFocus) {
		ErrorUtil.checkNotNull(element, "element");
		this.element = element;
		this.mouseOverable = mouseOverable;
		this.allowsCheating = allowsCheating;
		this.canClickToFocus = canClickToFocus;
	}

	@Override
	public ITypedIngredient<?> getTypedIngredient() {
		return element.getTypedIngredient();
	}

	@Override
	public IElement getElement() {
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
			return doGetCheatItemStack(element.getTypedIngredient(), ingredientManager);
		}
		return ItemStack.EMPTY;
	}

	private static <T> ItemStack doGetCheatItemStack(ITypedIngredient<T> value, IIngredientManager ingredientManager) {
		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(value.getType());
		return ingredientHelper.getCheatItemStack(value.getIngredient());
	}
}
