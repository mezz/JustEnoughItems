package mezz.jei.api.recipe;

import javax.annotation.Nullable;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;

import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.ISubtypeRegistry;

/**
 * Helps get ItemStacks from common formats used in recipes.
 * Get the instance from {@link IJeiHelpers#getStackHelper()}.
 */
public interface IStackHelper {
	/**
	 * Expands an Ingredient into a list of ItemStacks.
	 */
	List<ItemStack> toItemStackList(Ingredient ingredient);

	/**
	 * Expands Ingredients, into a list of lists of ItemStacks.
	 */
	List<List<ItemStack>> expandRecipeIngredients(NonNullList<Ingredient> inputs);

	/**
	 * Similar to ItemStack.areItemStacksEqual but ignores NBT on items without subtypes, and uses the {@link ISubtypeRegistry}
	 *
	 * @since JEI 3.13.4
	 */
	boolean isEquivalent(@Nullable ItemStack lhs, @Nullable ItemStack rhs);
}
