package mezz.jei.plugins.vanilla.ingredients;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mezz.jei.util.ErrorUtil;
import mezz.jei.util.Log;
import mezz.jei.util.StackHelper;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class ItemStackListFactory {

	private ItemStackListFactory() {

	}

	public static List<ItemStack> create(StackHelper stackHelper) {
		final List<ItemStack> itemList = new ArrayList<ItemStack>();
		final Set<String> itemNameSet = new HashSet<String>();

		for (CreativeTabs creativeTab : CreativeTabs.CREATIVE_TAB_ARRAY) {
			NonNullList<ItemStack> creativeTabItemStacks = NonNullList.create();
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
				} else if (itemStack.isEmpty()) {
					Log.error("Found an empty itemStack from creative tab: {}", creativeTab);
				} else {
					addItemStack(stackHelper, itemStack, itemList, itemNameSet);
				}
			}
		}

		for (Block block : ForgeRegistries.BLOCKS) {
			addBlockAndSubBlocks(stackHelper, block, itemList, itemNameSet);
		}

		for (Item item : ForgeRegistries.ITEMS) {
			addItemAndSubItems(stackHelper, item, itemList, itemNameSet);
		}

		return itemList;
	}

	private static void addItemAndSubItems(StackHelper stackHelper, @Nullable Item item, List<ItemStack> itemList, Set<String> itemNameSet) {
		if (item == null || item == Items.AIR) {
			return;
		}

		List<ItemStack> items = stackHelper.getSubtypes(item, 1);
		for (ItemStack stack : items) {
			if (stack != null) {
				addItemStack(stackHelper, stack, itemList, itemNameSet);
			}
		}
	}

	private static void addBlockAndSubBlocks(StackHelper stackHelper, @Nullable Block block, List<ItemStack> itemList, Set<String> itemNameSet) {
		if (block == null) {
			return;
		}

		Item item = Item.getItemFromBlock(block);
		if (item == Items.AIR) {
			return;
		}

		for (CreativeTabs itemTab : item.getCreativeTabs()) {
			NonNullList<ItemStack> subBlocks = NonNullList.create();
			try {
				block.getSubBlocks(item, itemTab, subBlocks);
			} catch (RuntimeException e) {
				String itemStackInfo = ErrorUtil.getItemStackInfo(new ItemStack(item));
				Log.error("Failed to getSubBlocks {}", itemStackInfo, e);
			} catch (LinkageError e) {
				String itemStackInfo = ErrorUtil.getItemStackInfo(new ItemStack(item));
				Log.error("Failed to getSubBlocks {}", itemStackInfo, e);
			}

			for (ItemStack subBlock : subBlocks) {
				if (subBlock == null) {
					Log.error("Found null subBlock of {}", block);
				} else if (subBlock.isEmpty()) {
					Log.error("Found empty subBlock of {}", block);
				} else {
					addItemStack(stackHelper, subBlock, itemList, itemNameSet);
				}
			}
		}
	}

	private static void addItemStack(StackHelper stackHelper, ItemStack stack, List<ItemStack> itemList, Set<String> itemNameSet) {
		String itemKey = null;

		try {
			itemKey = stackHelper.getUniqueIdentifierForStack(stack, StackHelper.UidMode.FULL);
		} catch (RuntimeException e) {
			String stackInfo = ErrorUtil.getItemStackInfo(stack);
			Log.error("Couldn't get unique name for itemStack {}", stackInfo, e);
		} catch (LinkageError e) {
			String stackInfo = ErrorUtil.getItemStackInfo(stack);
			Log.error("Couldn't get unique name for itemStack {}", stackInfo, e);
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
