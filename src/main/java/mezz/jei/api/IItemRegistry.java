package mezz.jei.api;

import com.google.common.collect.ImmutableList;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

/**
 * The IItemRegistry is provided by JEI and has some useful functions related to items.
 * Get the instance from {@link IModRegistry#getItemRegistry()}.
 */
public interface IItemRegistry {

	/**
	 * Returns a list of all the ItemStacks known to JEI, including their sub-items.
	 */
	@Nonnull
	ImmutableList<ItemStack> getItemList();

	/**
	 * Returns a list of all the ItemStacks that can be used as fuel in a vanilla furnace.
	 */
	@Nonnull
	ImmutableList<ItemStack> getFuels();

	/**
	 * Returns a list of all the ItemStacks that return true to isPotionIngredient.
	 */
	@Nonnull
	ImmutableList<ItemStack> getPotionIngredients();

	/**
	 * Returns a mod name for the given item.
	 */
	@Nonnull
	String getModNameForItem(@Nonnull Item item);

	/**
	 * Returns all the items registered by a specific mod.
	 */
	@Nonnull
	ImmutableList<ItemStack> getItemListForModId(@Nonnull String modId);
}
