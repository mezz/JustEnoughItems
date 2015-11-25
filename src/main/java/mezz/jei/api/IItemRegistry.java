package mezz.jei.api;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public interface IItemRegistry {

	/** Returns a list of all the Items registered. */
	@Nonnull
	ImmutableList<ItemStack> getItemList();

	/** Returns a list of all the Items that can be used as fuel in a vanilla furnace. */
	@Nonnull
	ImmutableList<ItemStack> getFuels();

	/** Returns a mod name for the given item. */
	@Nonnull
	String getModNameForItem(Item item);
}
