package mezz.jei.api.recipe;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import net.minecraft.item.ItemStack;

/**
 * Helps get ItemStacks from common formats used in recipes.
 */
public interface IStackHelper {
	/**
	 * Returns all the subtypes of itemStack if it has a wildcard meta value.
	 */
	@Nonnull
	List<ItemStack> getSubtypes(@Nonnull ItemStack itemStack);

	/**
	 * Expands an Iterable, which may contain ItemStacks or more Iterables, and
	 * returns all the subtypes of itemStacks if they have wildcard meta value.
	 */
	@Nonnull
	List<ItemStack> getAllSubtypes(@Nonnull Iterable stacks);

	/**
	 * Flattens ItemStacks, OreDict Strings, and Iterables into a list of ItemStacks.
	 */
	@Nonnull
	List<ItemStack> toItemStackList(@Nullable Object stacks);
}
