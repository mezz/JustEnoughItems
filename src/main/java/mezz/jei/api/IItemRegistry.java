package mezz.jei.api;

import com.google.common.collect.ImmutableList;
import mezz.jei.api.ingredients.IIngredientRegistry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * The IItemRegistry is provided by JEI and has some useful functions related to items.
 * Get the instance from {@link IModRegistry#getItemRegistry()}.
 * @deprecated since JEI 3.11.0. Use {@link IIngredientRegistry}.
 */
@Deprecated
public interface IItemRegistry {

	/**
	 * Returns a list of all the ItemStacks known to JEI, including their sub-items.
	 * @deprecated since JEI 3.11.0. Use {@link IIngredientRegistry#getIngredients(Class)} with ItemStack.class
	 */
	@Deprecated
	ImmutableList<ItemStack> getItemList();

	/**
	 * Returns a list of all the ItemStacks that can be used as fuel in a vanilla furnace.
	 * @deprecated since JEI 3.11.0. Use {@link IIngredientRegistry#getFuels()}.
	 */
	@Deprecated
	ImmutableList<ItemStack> getFuels();

	/**
	 * Returns a list of all the ItemStacks that return true to isPotionIngredient.
	 * @deprecated since JEI 3.11.0. Use {@link IIngredientRegistry#getPotionIngredients()}.
	 */
	@Deprecated
	ImmutableList<ItemStack> getPotionIngredients();

	/**
	 * Returns a mod name for the given item.
	 * @deprecated since JEI 3.11.0.
	 */
	@Deprecated
	String getModNameForItem(Item item);

	/**
	 * Returns a mod name for the given mod id.
	 * @deprecated since JEI 3.11.0.
	 */
	@Deprecated
	String getModNameForModId(String modId);

	/**
	 * Returns all the items registered by a specific mod.
	 * @deprecated since JEI 3.11.0.
	 */
	@Deprecated
	ImmutableList<ItemStack> getItemListForModId(String modId);
}
