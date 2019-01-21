package mezz.jei.api.recipe;

import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;

import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.ingredients.ISubtypeRegistry;

/**
 * Helps get ItemStacks from common formats used in recipes.
 * Get the instance from {@link IJeiHelpers#getStackHelper()}.
 */
public interface IStackHelper {
	/**
	 * Similar to ItemStack.areItemStacksEqual but ignores NBT on items without subtypes, and uses the {@link ISubtypeRegistry}
	 */
	boolean isEquivalent(@Nullable ItemStack lhs, @Nullable ItemStack rhs);
}
