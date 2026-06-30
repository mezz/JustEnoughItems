package mezz.jei.neoforge.tests.lib;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public record StackPlacement(int index, ItemStack stack) {
	public static StackPlacement stackAt(int index, ItemStack stack) {
		return new StackPlacement(index, stack);
	}

	public static StackPlacement stackAt(int index, ItemLike itemLike) {
		return new StackPlacement(index, new ItemStack(itemLike));
	}
}
