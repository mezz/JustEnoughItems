package mezz.jei.common.util;

import net.minecraft.world.item.ItemStack;

public enum GiveAmount {
	ONE, MAX;

	public int getAmountForStack(ItemStack itemStack) {
		return switch (this) {
			case MAX -> itemStack.getMaxStackSize();
			case ONE -> 1;
		};
	}
}
