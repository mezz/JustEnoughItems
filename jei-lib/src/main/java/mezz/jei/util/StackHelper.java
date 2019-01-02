package mezz.jei.util;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

import mezz.jei.api.ISubtypeRegistry;
import mezz.jei.api.recipe.IStackHelper;

public class StackHelper implements IStackHelper {
	private final ISubtypeRegistry subtypeRegistry;

	public StackHelper(ISubtypeRegistry subtypeRegistry) {
		this.subtypeRegistry = subtypeRegistry;
	}

	/**
	 * Similar to ItemStack.areItemStacksEqual but ignores NBT on items without subtypes, and uses the {@link ISubtypeRegistry}
	 */
	@Override
	public boolean isEquivalent(@Nullable ItemStack lhs, @Nullable ItemStack rhs) {
		if (lhs == rhs) {
			return true;
		}

		if (lhs == null || rhs == null) {
			return false;
		}

		if (lhs.getItem() != rhs.getItem()) {
			return false;
		}

		String keyLhs = getUniqueIdentifierForStack(lhs, UidMode.NORMAL);
		String keyRhs = getUniqueIdentifierForStack(rhs, UidMode.NORMAL);
		return keyLhs.equals(keyRhs);
	}

	@Override
	public List<List<ItemStack>> expandRecipeIngredients(NonNullList<Ingredient> inputs) {
		List<List<ItemStack>> expandedInputs = new ArrayList<>();
		for (Ingredient input : inputs) {
			List<ItemStack> expandedInput = toItemStackList(input);
			expandedInputs.add(expandedInput);
		}
		return expandedInputs;
	}

	@Override
	public List<ItemStack> toItemStackList(Ingredient ingredient) {
		ItemStack[] stacks = ingredient.getMatchingStacks();
		return Arrays.asList(stacks);
	}

	public String getUniqueIdentifierForStack(ItemStack stack) {
		return getUniqueIdentifierForStack(stack, UidMode.NORMAL);
	}

	public String getUniqueIdentifierForStack(ItemStack stack, UidMode mode) {
		ErrorUtil.checkNotEmpty(stack, "stack");

		Item item = stack.getItem();
		ResourceLocation registryName = item.getRegistryName();
		if (registryName == null) {
			String stackInfo = ErrorUtil.getItemStackInfo(stack);
			throw new IllegalStateException("Item has no registry name: " + stackInfo);
		}

		String result = registryName.toString();
		if (mode == UidMode.NORMAL) {
			String subtypeInfo = subtypeRegistry.getSubtypeInfo(stack);
			if (subtypeInfo != null && !subtypeInfo.isEmpty()) {
				result = result + ':' + subtypeInfo;
			}
		}
		return result;
	}

	public enum UidMode {
		NORMAL, WILDCARD
	}
}
