package mezz.jei.input;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.util.ErrorUtil;
import net.minecraft.item.ItemStack;

public class ClickedIngredient<V> implements IClickedIngredient<V> {
	private final V value;
	private boolean allowsCheating;

	public ClickedIngredient(V value) {
		ErrorUtil.checkIsValidIngredient(value, "value");
		this.value = value;
	}

	@Override
	public V getValue() {
		return value;
	}

	public void setAllowsCheating() {
		this.allowsCheating = true;
	}

	@Override
	public ItemStack getCheatItemStack(IIngredientRegistry ingredientRegistry) {
		if (allowsCheating) {
			IIngredientHelper<V> ingredientHelper = ingredientRegistry.getIngredientHelper(value);
			return ingredientHelper.getCheatItemStack(value);
		}
		return ItemStack.EMPTY;
	}
}
