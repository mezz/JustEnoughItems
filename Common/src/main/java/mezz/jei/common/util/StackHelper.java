package mezz.jei.common.util;

import mezz.jei.api.helpers.IStackHelper;
import mezz.jei.api.ingredients.subtypes.ISubtypeManager;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.common.platform.IPlatformRegistry;
import mezz.jei.common.platform.Services;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

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
		if (!subtypeInfo.isEmpty()) {
			result = result + ':' + subtypeInfo;
		}
		return result;
	}

	public static String getRegistryNameForStack(ItemStack stack) {
		ErrorUtil.checkNotEmpty(stack, "stack");

		Item item = stack.getItem();
		IPlatformRegistry<Item> itemRegistry = Services.PLATFORM.getRegistry(Registry.ITEM_REGISTRY);
		ResourceLocation registryName = itemRegistry.getRegistryName(item);
		if (registryName == null) {
			String stackInfo = ErrorUtil.getItemStackInfo(stack);
			throw new IllegalStateException("Item has no registry name: " + stackInfo);
		}

		return registryName.toString();
	}
}
