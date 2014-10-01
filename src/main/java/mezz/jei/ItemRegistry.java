package mezz.jei;

import cpw.mods.fml.common.registry.GameData;
import mezz.jei.util.Log;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;

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
		if (item == null)
			return;

		ArrayList<ItemStack> subItems = new ArrayList<ItemStack>();
		item.getSubItems(item, null, subItems);
		addItemStacks(subItems);

		if (subItems.isEmpty()) {
			ItemStack stack = new ItemStack(item);
			if (stack.getItem() == null) {
				Log.warning("Empty item stack from item: " + item.getUnlocalizedName());
				return;
			}
			addItemStack(stack);
		}
	}

	public void addBlockAndSubBlocks(Block block) {
		if (block == null)
			return;

		Item item = Item.getItemFromBlock(block);

		ArrayList<ItemStack> subItems = new ArrayList<ItemStack>();
		if (item != null) {
			block.getSubBlocks(item, null, subItems);
			addItemStacks(subItems);
		}

		if (subItems.isEmpty()) {
			ItemStack stack = new ItemStack(block);
			if (stack.getItem() == null) {
				Log.debug("Couldn't get itemStack for block: " + block.getUnlocalizedName());
				return;
			}
			addItemStack(stack);
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
		StringBuilder itemKey = new StringBuilder(stack.getUnlocalizedName());
		if (stack.hasTagCompound())
			itemKey.append(":").append(stack.getTagCompound().toString());
		return itemKey.toString();
	}

}
