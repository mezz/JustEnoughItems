package mezz.jei.api;

import com.google.common.collect.ImmutableList;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * The IItemRegistry is provided by JEI and has some useful functions related to items.
 * Get the instance from {@link IModRegistry#getItemRegistry()}.
 */
public interface IItemRegistry {

	/**
	 * Returns a list of all the ItemStacks known to JEI, including their sub-items.
	 */
	ImmutableList<ItemStack> getItemList();

	/**
	 * Returns a list of all the ItemStacks that can be used as fuel in a vanilla furnace.
	 */
	ImmutableList<ItemStack> getFuels();

	/**
	 * Returns a list of all the ItemStacks that return true to isPotionIngredient.
	 */
	ImmutableList<ItemStack> getPotionIngredients();

	/**
	 * Returns a mod name for the given item.
	 */
	String getModNameForItem(Item item);

	/**
	 * Returns a mod name for the given mod id.
	 */
	String getModNameForModId(String modId);

	/**
	 * Returns all the items registered by a specific mod.
	 */
	ImmutableList<ItemStack> getItemListForModId(String modId);
}
