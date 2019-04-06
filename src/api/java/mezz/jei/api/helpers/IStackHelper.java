package mezz.jei.api.helpers;

import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;

import mezz.jei.api.ingredients.subtypes.ISubtypeManager;

/**
 * Helps get ItemStacks from common formats used in recipes.
 * Get the instance from {@link IJeiHelpers#getStackHelper()}.
 */
public interface IStackHelper {
	/**
	 * Similar to ItemStack.areItemStacksEqual but ignores NBT on items without subtypes, and uses the {@link ISubtypeManager}
	 */
	boolean isEquivalent(@Nullable ItemStack lhs, @Nullable ItemStack rhs);
}
