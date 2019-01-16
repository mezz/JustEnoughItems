package mezz.jei.api;

import net.minecraftforge.oredict.OreDictionary;
import net.minecraft.item.ItemStack;

import mezz.jei.api.ingredients.IIngredientBlacklist;

/**
 * The Item Blacklist allows mods to hide their items from JEI's item list.
 * Get the instance from {@link IJeiHelpers#getItemBlacklist()}.
 *
 * @deprecated Since JEI 4.2.1. Use {@link IIngredientBlacklist}.
 */
@Deprecated
public interface IItemBlacklist {
	/**
	 * Stop JEI from displaying a specific item in the item list.
	 * Use {@link OreDictionary#WILDCARD_VALUE} meta for wildcard.
	 * Items blacklisted with this API can't be seen in the config or in hide ingredients mode.
	 *
	 * @deprecated Since JEI 4.2.1. Use {@link IIngredientBlacklist#addIngredientToBlacklist(Object)}.
	 */
	@Deprecated
	void addItemToBlacklist(ItemStack itemStack);

	/**
	 * Undo blacklisting an item.
	 * This is for mods that hide items initially and reveal them when certain conditions are met.
	 * Items blacklisted by the user in the config will remain hidden.
	 *
	 * @deprecated Since JEI 4.2.1. Use {@link IIngredientBlacklist#removeIngredientFromBlacklist(Object)}.
	 */
	@Deprecated
	void removeItemFromBlacklist(ItemStack itemStack);

	/**
	 * Returns true if the item is blacklisted and will not be displayed in the item list.
	 *
	 * @deprecated Since JEI 4.2.1. Use {@link IIngredientBlacklist#isIngredientBlacklisted(Object)}.
	 */
	@Deprecated
	boolean isItemBlacklisted(ItemStack itemStack);
}
