package mezz.jei.api;

import net.minecraft.item.ItemStack;

public interface IItemBlacklist {
	/**
	 * Stop JEI from displaying a specific item in the item list.
	 * Use OreDictionary.WILDCARD_VALUE meta for wildcard.
	 * Items blacklisted with this API can't be seen in the config or in edit mode.
	 */
	void addItemToBlacklist(ItemStack itemStack);

	/**
	 * Undo blacklisting an item.
	 * This is for mods that hide items initially and reveal them when certain conditions are met.
	 * Items blacklisted by the user in the config will remain hidden.
	 */
	void removeItemFromBlacklist(ItemStack itemStack);

	/** Returns true if the item is blacklisted and will not be displayed in the item list. */
	boolean isItemBlacklisted(ItemStack itemStack);
}
