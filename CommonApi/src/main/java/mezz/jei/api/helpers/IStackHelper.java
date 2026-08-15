package mezz.jei.api.helpers;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.world.item.ItemStack;

import mezz.jei.api.ingredients.subtypes.ISubtypeManager;

/**
 * Helps get ItemStacks from common formats used in recipes.
 * Get the instance from {@link IJeiHelpers#getStackHelper()}.
 */
@ApiStatus.NonExtendable
public interface IStackHelper {
	/**
	 * Gets the unique identifier for a stack, ignoring NBT on items without subtypes, and uses the {@link ISubtypeManager}.
	 * If two unique identifiers are equal, then the items can be considered equivalent.
	 *
	 * Returns an {@link Object} so that UID creation can be optimized.
	 * Make sure the returned value implements {@link Object#equals(Object)} and {@link Object#hashCode()}.
	 *
	 * @since 15.49.0
	 */
	default Object getUidForStack(ItemStack stack, UidContext context) {
		return getUniqueIdentifierForStack(stack, context);
	}

	/**
	 * Similar to ItemStack.areItemStacksEqual but ignores NBT on items without subtypes, and uses the {@link ISubtypeManager}
	 * @since 7.3.0
	 */
	boolean isEquivalent(@Nullable ItemStack lhs, @Nullable ItemStack rhs, UidContext context);

	/**
	 * Gets the unique identifier for a stack, ignoring NBT on items without subtypes, and uses the {@link ISubtypeManager}.
	 * If two unique identifiers are equal, then the items can be considered equivalent.
	 * @since 7.6.1
	 */
	String getUniqueIdentifierForStack(ItemStack stack, UidContext context);
}
