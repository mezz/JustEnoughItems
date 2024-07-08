package mezz.jei.library.plugins.vanilla.ingredients;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.NonNullList;

import mezz.jei.common.util.ErrorUtil;
import mezz.jei.common.util.StackHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class ItemStackListFactory {
	private static final Logger LOGGER = LogManager.getLogger();

	public static List<ItemStack> create(StackHelper stackHelper) {
		final List<ItemStack> itemList = new ArrayList<>();
		final Set<Object> itemUidSet = new HashSet<>();

		for (CreativeModeTab itemGroup : CreativeModeTab.TABS) {
			if (itemGroup == CreativeModeTab.TAB_HOTBAR || itemGroup == CreativeModeTab.TAB_INVENTORY) {
				continue;
			}
			NonNullList<ItemStack> creativeTabItemStacks = NonNullList.create();
			try {
				itemGroup.fillItemList(creativeTabItemStacks);
			} catch (RuntimeException | LinkageError e) {
				LOGGER.error("Item Group crashed while getting items." +
					"Some items from this group will be missing from the ingredient list. {}", itemGroup, e);
			}
			for (ItemStack itemStack : creativeTabItemStacks) {
				if (itemStack.isEmpty()) {
					LOGGER.error("Found an empty itemStack from creative tab: {}", itemGroup);
				} else {
					addItemStack(stackHelper, itemStack, itemList, itemUidSet);
				}
			}
		}
		return itemList;
	}

	private static void addItemStack(StackHelper stackHelper, ItemStack stack, List<ItemStack> itemList, Set<Object> itemUidSet) {
		final Object itemKey;

		if (stackHelper.hasSubtypes(stack)) {
			try {
				itemKey = stackHelper.getUniqueIdentifierForStack(stack, UidContext.Ingredient);
			} catch (RuntimeException | LinkageError e) {
				String stackInfo = ErrorUtil.getItemStackInfo(stack);
				LOGGER.error("Couldn't get unique name for itemStack {}", stackInfo, e);
				return;
			}
		} else {
			itemKey = stack.getItem();
		}

		if (!itemUidSet.contains(itemKey)) {
			itemUidSet.add(itemKey);
			itemList.add(stack);
		}
	}

}
