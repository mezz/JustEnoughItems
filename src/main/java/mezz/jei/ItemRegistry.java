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
			addItemStackAndSubtypes(new ItemStack(block));

		for (Item item : GameData.getItemRegistry().typeSafeIterable())
			addItemStackAndSubtypes(new ItemStack(item));

		for (ItemStack stack : itemList)
			Log.info(stack.getUnlocalizedName());
	}

	public void addItemStackAndSubtypes(ItemStack itemStack) {
		if (itemStack == null)
			return;

		Item item = itemStack.getItem();

		if (item == null)
			return;

		if (itemStack.getHasSubtypes()) {
			ArrayList<ItemStack> subItems = new ArrayList<ItemStack>();
			item.getSubItems(item, null, subItems);
			addItemStacks(subItems);
		} else {
			addItemStack(itemStack);
		}
	}

	public void addItemStacks(Iterable<ItemStack> stacks) {
		for (ItemStack stack : stacks)
			addItemStack(stack);
	}

	public void addItemStack(ItemStack stack) {
		if (itemNameSet.contains(stack.getUnlocalizedName()))
			return;
		itemNameSet.add(stack.getUnlocalizedName());
		itemList.add(stack);
	}


}
