package mezz.jei.api.helpers;

import javax.annotation.Nullable;

import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.item.ItemStack;

import mezz.jei.api.ingredients.subtypes.ISubtypeManager;

/**
 * Helps get ItemStacks from common formats used in recipes.
 * Get the instance from {@link IJeiHelpers#getStackHelper()}.
 */
public interface IStackHelper {
	/**
	 * Similar to ItemStack.areItemStacksEqual but ignores NBT on items without subtypes, and uses the {@link ISubtypeManager}
	 * @since JEI 7.3.0
	 */
	boolean isEquivalent(@Nullable ItemStack lhs, @Nullable ItemStack rhs, UidContext context);

	/**
	 * Similar to ItemStack.areItemStacksEqual but ignores NBT on items without subtypes, and uses the {@link ISubtypeManager}
	 * @deprecated since JEI 7.3.0. Use {@link #isEquivalent(ItemStack, ItemStack, UidContext)}
	 */
	@Deprecated
	default boolean isEquivalent(@Nullable ItemStack lhs, @Nullable ItemStack rhs) {
		return isEquivalent(lhs, rhs, UidContext.Ingredient);
	}
}
