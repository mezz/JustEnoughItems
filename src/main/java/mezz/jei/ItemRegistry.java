package mezz.jei;

import cpw.mods.fml.common.registry.GameData;
import mezz.jei.util.Log;
import mezz.jei.util.StackUtil;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class ItemRegistry {

	public ArrayList<ItemStack> itemList = new ArrayList<ItemStack>();
	public HashSet<String> itemNameSet = new HashSet<String>();

	public ItemRegistry() {

		for (Block block : GameData.getBlockRegistry().typeSafeIterable())
			addBlockAndSubBlocks(block);

		for (Item item : GameData.getItemRegistry().typeSafeIterable())
			addItemAndSubItems(item);
	}

	public void addItemAndSubItems(Item item) {
		List<ItemStack> items = StackUtil.getSubItems(item);
		addItemStacks(items);
	}

	public void addBlockAndSubBlocks(Block block) {
		if (block == null)
			return;

		Item item = Item.getItemFromBlock(block);

		if (item == null) {
			ItemStack stack = new ItemStack(block);
			if (stack.getItem() == null) {
				Log.debug("Couldn't get itemStack for block: " + block.getUnlocalizedName());
				return;
			}
			addItemStack(stack);
			return;
		}

		ArrayList<ItemStack> subItems = new ArrayList<ItemStack>();
		for (CreativeTabs itemTab : item.getCreativeTabs()) {
			subItems.clear();
			block.getSubBlocks(item, itemTab, subItems);
			addItemStacks(subItems);

			if (subItems.isEmpty()) {
				ItemStack stack = new ItemStack(block);
				if (stack.getItem() == null) {
					Log.debug("Couldn't get itemStack for block: " + block.getUnlocalizedName());
					return;
				}
				addItemStack(stack);
			}
		}
	}

	public void addItemStacks(Iterable<ItemStack> stacks) {
		for (ItemStack stack : stacks)
			addItemStack(stack);
	}

	public void addItemStack(ItemStack stack) {
		String itemKey = uniqueIdentifierForStack(stack);

		if (itemNameSet.contains(itemKey))
			return;
		itemNameSet.add(itemKey);
		itemList.add(stack);
	}

	private String uniqueIdentifierForStack(ItemStack stack) {
		int id = GameData.getItemRegistry().getId(stack.getItem());
		StringBuilder itemKey = new StringBuilder();
		itemKey.append(id).append(":").append(stack.getItemDamage());
		if (stack.hasTagCompound())
			itemKey.append(":").append(stack.getTagCompound().toString());
		return itemKey.toString();
	}

}
