package mezz.jei.api.recipe;

import javax.annotation.Nullable;
import java.util.List;

import mezz.jei.api.IJeiHelpers;
import net.minecraft.item.ItemStack;

/**
 * Helps get ItemStacks from common formats used in recipes.
 * Get the instance from {@link IJeiHelpers#getStackHelper()}.
 */
public interface IStackHelper {
	/**
	 * Returns all the subtypes of itemStack if it has a wildcard meta value.
	 */
	List<ItemStack> getSubtypes(ItemStack itemStack);

	/**
	 * Expands an Iterable, which may contain ItemStacks or more Iterables, and
	 * returns all the subtypes of itemStacks if they have wildcard meta value.
	 */
	List<ItemStack> getAllSubtypes(Iterable stacks);

	/**
	 * Flattens ItemStacks, OreDict Strings, and Iterables into a list of ItemStacks.
	 */
	List<ItemStack> toItemStackList(@Nullable Object stacks);
}
