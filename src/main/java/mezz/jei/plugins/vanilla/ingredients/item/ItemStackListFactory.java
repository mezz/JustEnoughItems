package mezz.jei.plugins.vanilla.ingredients.item;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.NonNullList;

import mezz.jei.util.ErrorUtil;
import mezz.jei.util.StackHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class ItemStackListFactory {
	private static final Logger LOGGER = LogManager.getLogger();

	public List<ItemStack> create(StackHelper stackHelper) {
		final List<ItemStack> itemList = new ArrayList<>();
		final Set<String> itemNameSet = new HashSet<>();

		for (ItemGroup itemGroup : ItemGroup.GROUPS) {
			if (itemGroup == ItemGroup.HOTBAR || itemGroup == ItemGroup.INVENTORY) {
				continue;
			}
			NonNullList<ItemStack> creativeTabItemStacks = NonNullList.create();
			try {
				itemGroup.fill(creativeTabItemStacks);
			} catch (RuntimeException | LinkageError e) {
				LOGGER.error("Item Group crashed while getting items." +
					"Some items from this group will be missing from the ingredient list. {}", itemGroup, e);
			}
			for (ItemStack itemStack : creativeTabItemStacks) {
				if (itemStack.isEmpty()) {
					LOGGER.error("Found an empty itemStack from creative tab: {}", itemGroup);
				} else {
					addItemStack(stackHelper, itemStack, itemList, itemNameSet);
				}
			}
		}
		return itemList;
	}

	private void addItemStack(StackHelper stackHelper, ItemStack stack, List<ItemStack> itemList, Set<String> itemNameSet) {
		// Game freezes when loading player skulls, see https://bugs.mojang.com/browse/MC-65587
		if (stack.getItem() == Items.PLAYER_HEAD) {
			return;
		}

		final String itemKey;

		try {
			itemKey = stackHelper.getUniqueIdentifierForStack(stack);
		} catch (RuntimeException | LinkageError e) {
			String stackInfo = ErrorUtil.getItemStackInfo(stack);
			LOGGER.error("Couldn't get unique name for itemStack {}", stackInfo, e);
			return;
		}

		if (!itemNameSet.contains(itemKey)) {
			itemNameSet.add(itemKey);
			itemList.add(stack);
		}
	}

}
