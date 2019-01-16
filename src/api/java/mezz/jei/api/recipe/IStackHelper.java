package mezz.jei.api.recipe;

import javax.annotation.Nullable;
import java.util.List;

import net.minecraft.item.ItemStack;

import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.ISubtypeRegistry;

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

	/**
	 * Expands ItemStacks, Ingredients, and Iterables into a list of lists of ItemStacks.
	 * Expands wildcard ItemStacks into their subtypes.
	 */
	List<List<ItemStack>> expandRecipeItemStackInputs(@Nullable List inputs);

	/**
	 * Returns an ItemStack from 'stacks' that matches any of the ItemStacks in 'contains'.
	 * Returns null if there is no match.
	 *
	 * @since JEI 3.13.4
	 */
	@Nullable
	ItemStack containsAnyStack(Iterable<ItemStack> stacks, Iterable<ItemStack> contains);

	/**
	 * Similar to ItemStack.areItemStacksEqual but ignores NBT on items without subtypes, and uses the {@link ISubtypeRegistry}
	 *
	 * @since JEI 3.13.4
	 */
	boolean isEquivalent(@Nullable ItemStack lhs, @Nullable ItemStack rhs);
}
