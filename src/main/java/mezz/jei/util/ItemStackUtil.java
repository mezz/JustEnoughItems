package mezz.jei.util;

import java.util.Collection;

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public final class ItemStackUtil {
	private ItemStackUtil() {

	}

	public static NonNullList<ItemStack> singletonList(ItemStack itemStack) {
		NonNullList<ItemStack> list = NonNullList.create();
		list.add(itemStack);
		return list;
	}

	public static NonNullList<ItemStack> toNonNullList(Collection<ItemStack> itemStacks) {
		if (itemStacks instanceof NonNullList) {
			return (NonNullList<ItemStack>) itemStacks;
		}

		NonNullList<ItemStack> list = NonNullList.create();
		list.addAll(itemStacks);
		return list;
	}
}
