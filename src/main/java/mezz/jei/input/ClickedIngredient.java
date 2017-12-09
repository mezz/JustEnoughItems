package mezz.jei.input;

import mezz.jei.Internal;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.util.ErrorUtil;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;
import java.awt.Rectangle;

public class ClickedIngredient<V> implements IClickedIngredient<V> {
	private final V value;
	@Nullable
	private final Rectangle area;
	@Nullable
	private IOnClickHandler onClickHandler;
	private boolean allowsCheating;

	public ClickedIngredient(V value, @Nullable Rectangle area) {
		ErrorUtil.checkIsValidIngredient(value, "value");
		this.value = value;
		this.area = area;
	}

	@Override
	public V getValue() {
		return value;
	}

	@Nullable
	@Override
	public Rectangle getArea() {
		return area;
	}

	public void setAllowsCheating() {
		this.allowsCheating = true;
	}

	@Override
	public void setOnClickHandler(IOnClickHandler onClickHandler) {
		this.onClickHandler = onClickHandler;
	}

	@Override
	public ItemStack getCheatItemStack() {
		if (allowsCheating) {
			IIngredientRegistry ingredientRegistry = Internal.getIngredientRegistry();
			IIngredientHelper<V> ingredientHelper = ingredientRegistry.getIngredientHelper(value);
			return ingredientHelper.getCheatItemStack(value);
		}
		return ItemStack.EMPTY;
	}

	@Override
	public void onClickHandled() {
		if (this.onClickHandler != null) {
			this.onClickHandler.onClick();
		}
	}
}
