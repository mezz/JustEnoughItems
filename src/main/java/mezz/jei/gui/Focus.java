package mezz.jei.gui;

import com.google.common.base.Preconditions;
import mezz.jei.api.recipe.IFocus;
import net.minecraft.item.ItemStack;

public class Focus<V> implements IFocus<V> {
	private final Mode mode;
	private final V value;

	public Focus(Mode mode, V value) {
		this.mode = mode;
		this.value = value;
		validate(this);
	}

	@Override
	public V getValue() {
		return value;
	}

	@Override
	public Mode getMode() {
		return mode;
	}

	public static void validate(IFocus<?> focus) {
		Preconditions.checkNotNull(focus.getMode(), "mode must not be null");
		Object value = focus.getValue();
		Preconditions.checkNotNull(value, "value must not be null");
		if (value instanceof ItemStack) {
			ItemStack itemStack = (ItemStack) value;
			Preconditions.checkArgument(!itemStack.isEmpty(), "ItemStack value must not be empty");
		}
	}
}
