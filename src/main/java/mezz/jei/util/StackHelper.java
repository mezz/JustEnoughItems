package mezz.jei.util;

import javax.annotation.Nullable;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import mezz.jei.api.helpers.IStackHelper;
import mezz.jei.api.ingredients.subtypes.ISubtypeManager;

public class StackHelper implements IStackHelper {
	private final ISubtypeManager subtypeManager;

	public StackHelper(ISubtypeManager subtypeManager) {
		this.subtypeManager = subtypeManager;
	}

	/**
	 * Similar to ItemStack.areItemStacksEqual but ignores NBT on items without subtypes, and uses the {@link ISubtypeManager}
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
			String subtypeInfo = subtypeManager.getSubtypeInfo(stack);
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
