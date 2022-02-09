package mezz.jei.util;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.item.ItemStack;

public interface ItemStackMatchable<R> {
	@Nullable
	ItemStack getStack();

	@Nullable
	R getResult();
}
