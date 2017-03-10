package mezz.jei.input;

import com.google.common.base.Preconditions;
import net.minecraft.item.ItemStack;

public class ClickedIngredient<V> implements IClickedIngredient<V> {
	private final V value;
	private boolean allowsCheating;

	public ClickedIngredient(V value) {
		Preconditions.checkNotNull(value, "value must not be null");
		if (value instanceof ItemStack) {
			ItemStack itemStack = (ItemStack) value;
			Preconditions.checkArgument(!itemStack.isEmpty(), "ItemStack value must not be empty");
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
