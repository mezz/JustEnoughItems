package mezz.jei.util;

import mezz.jei.config.KeyBindings;
import mezz.jei.input.UserInput;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

public enum GiveAmount {
	ONE, MAX;

	@Nullable
	public static GiveAmount getGiveAmount(UserInput input) {
		if (input.is(KeyBindings.cheatItemStack)) {
			return MAX;
		} else if (input.is(KeyBindings.cheatOneItem)) {
			return ONE;
		}
		return null;
	}

	public int getAmountForStack(ItemStack itemStack) {
		return switch (this) {
			case MAX -> itemStack.getMaxStackSize();
			case ONE -> 1;
		};
	}
}
