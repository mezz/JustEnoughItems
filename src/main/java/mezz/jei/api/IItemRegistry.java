package mezz.jei.api;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * The IItemRegistry is provided by JEI and has some useful functions related to items.
 */
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

	/**
	 * Stop JEI from displaying a specific item in the item list.
	 * Use OreDictionary.WILDCARD_VALUE meta for wildcard.
	 * Items blacklisted with this API can't be seen in the config or in edit mode.
	 */
	void addItemToBlacklist(ItemStack itemStack);

	/** Returns true if the item is blacklisted and will not be displayed in the item list. */
	boolean isItemBlacklisted(ItemStack itemStack);
}
