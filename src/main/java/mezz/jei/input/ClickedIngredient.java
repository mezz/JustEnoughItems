package mezz.jei.input;

import mezz.jei.util.ErrorUtil;
import net.minecraft.item.ItemStack;

public class ClickedIngredient<V> implements IClickedIngredient<V> {
	private final V value;
	private boolean allowsCheating;

	public ClickedIngredient(V value) {
		ErrorUtil.checkNotNull(value, "value");
		if (value instanceof ItemStack) {
			ItemStack itemStack = (ItemStack) value;
			ErrorUtil.checkNotEmpty(itemStack);
		}
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
	public boolean allowsCheating() {
		return allowsCheating;
	}
}
