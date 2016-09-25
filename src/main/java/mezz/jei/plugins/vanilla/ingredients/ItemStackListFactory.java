package mezz.jei.plugins.vanilla.ingredients;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mezz.jei.Internal;
import mezz.jei.util.ErrorUtil;
import mezz.jei.util.Log;
import mezz.jei.util.StackHelper;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class ItemStackListFactory {

	private ItemStackListFactory() {

	}

	public static List<ItemStack> create() {
		final List<ItemStack> itemList = new ArrayList<ItemStack>();
		final Set<String> itemNameSet = new HashSet<String>();

		for (CreativeTabs creativeTab : CreativeTabs.CREATIVE_TAB_ARRAY) {
			List<ItemStack> creativeTabItemStacks = new ArrayList<ItemStack>();
			try {
				creativeTab.displayAllRelevantItems(creativeTabItemStacks);
			} catch (RuntimeException e) {
				Log.error("Creative tab crashed while getting items. Some items from this tab will be missing from the item list. {}", creativeTab, e);
			} catch (LinkageError e) {
				Log.error("Creative tab crashed while getting items. Some items from this tab will be missing from the item list. {}", creativeTab, e);
			}
			for (ItemStack itemStack : creativeTabItemStacks) {
				if (itemStack == null) {
					Log.error("Found a null itemStack in creative tab: {}", creativeTab);
				} else if (itemStack.getItem() == null) {
					Log.error("Found a null item in an itemStack from creative tab: {}", creativeTab);
				} else {
					addItemStack(itemStack, itemList, itemNameSet);
				}
			}
		}

		for (Block block : ForgeRegistries.BLOCKS) {
			addBlockAndSubBlocks(block, itemList, itemNameSet);
		}

		for (Item item : ForgeRegistries.ITEMS) {
			addItemAndSubItems(item, itemList, itemNameSet);
		}

		return itemList;
	}

	private static void addItemAndSubItems(@Nullable Item item, List<ItemStack> itemList, Set<String> itemNameSet) {
		if (item == null) {
			return;
		}

		List<ItemStack> items = Internal.getStackHelper().getSubtypes(item, 1);
		for (ItemStack stack : items) {
			if (stack != null) {
				addItemStack(stack, itemList, itemNameSet);
			}
		}
	}

	private static void addBlockAndSubBlocks(@Nullable Block block, List<ItemStack> itemList, Set<String> itemNameSet) {
		if (block == null) {
			return;
		}

		Item item = Item.getItemFromBlock(block);
		if (item == null) {
			return;
		}

		for (CreativeTabs itemTab : item.getCreativeTabs()) {
			List<ItemStack> subBlocks = new ArrayList<ItemStack>();
			block.getSubBlocks(item, itemTab, subBlocks);
			for (ItemStack subBlock : subBlocks) {
				if (subBlock == null) {
					Log.error("Found null subBlock of {}", block);
				} else if (subBlock.getItem() == null) {
					Log.error("Found subBlock of {} with null item", block);
				} else {
					addItemStack(subBlock, itemList, itemNameSet);
				}
			}
		}
	}

	private static void addItemStack(ItemStack stack, List<ItemStack> itemList, Set<String> itemNameSet) {
		StackHelper stackHelper = Internal.getStackHelper();
		String itemKey = null;

		try {
			itemKey = stackHelper.getUniqueIdentifierForStack(stack, StackHelper.UidMode.FULL);
		} catch (RuntimeException e) {
			String stackInfo = ErrorUtil.getItemStackInfo(stack);
			Log.error("Couldn't create unique name for itemStack {}", stackInfo, e);
		}

		if (itemKey != null) {
			if (itemNameSet.contains(itemKey)) {
				return;
			}
			itemNameSet.add(itemKey);
			itemList.add(stack);
		}
	}
}
