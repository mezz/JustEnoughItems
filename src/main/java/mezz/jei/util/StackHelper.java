package mezz.jei.util;

import javax.annotation.Nullable;

import mezz.jei.api.ingredients.subtypes.UidContext;
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

	@Override
	public boolean isEquivalent(@Nullable ItemStack lhs, @Nullable ItemStack rhs, UidContext context) {
		ErrorUtil.checkNotNull(context, "context");
		if (lhs == rhs) {
			return true;
		}

		if (lhs == null || rhs == null) {
			return false;
		}

		if (lhs.getItem() != rhs.getItem()) {
			return false;
		}

		String keyLhs = getUniqueIdentifierForStack(lhs, context);
		String keyRhs = getUniqueIdentifierForStack(rhs, context);
		return keyLhs.equals(keyRhs);
	}

	@Override
	public String getUniqueIdentifierForStack(ItemStack stack, UidContext context) {
		String result = getRegistryNameForStack(stack);
		String subtypeInfo = subtypeManager.getSubtypeInfo(stack, context);
		if (subtypeInfo != null && !subtypeInfo.isEmpty()) {
			result = result + ':' + subtypeInfo;
		}
		return result;
	}

	public String getRegistryNameForStack(ItemStack stack) {
		ErrorUtil.checkNotEmpty(stack, "stack");

		Item item = stack.getItem();
		ResourceLocation registryName = item.getRegistryName();
		if (registryName == null) {
			String stackInfo = ErrorUtil.getItemStackInfo(stack);
			throw new IllegalStateException("Item has no registry name: " + stackInfo);
		}

		return registryName.toString();
	}

	public enum UidMode {
		NORMAL, WILDCARD
	}
}
