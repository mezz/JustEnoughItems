package mezz.jei.util;

import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;

public interface ItemStackMatchable<R> {
	@Nullable
	ItemStack getStack();

	@Nullable
	R getResult();
}
