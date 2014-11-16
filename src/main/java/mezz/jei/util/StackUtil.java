package mezz.jei.util;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.List;

public class StackUtil {

	public static List<ItemStack> removeDuplicateItemStacks(Iterable<ItemStack> stacks) {
		ArrayList<ItemStack> newStacks = new ArrayList<ItemStack>();
		if (stacks == null)
			return newStacks;

		for (ItemStack stack : stacks) {
			if (stack != null && containsStack(newStacks, stack) == null)
				newStacks.add(stack);
		}
		return newStacks;
	}

	/* Returns an ItemStack from "stacks" if it isIdentical to "contains" */
	public static ItemStack containsStack(Iterable<ItemStack> stacks, ItemStack contains) {
		if (stacks == null || contains == null)
			return null;

		for (ItemStack stack : stacks) {
			if (isIdentical(stack, contains))
				return stack;
		}
		return null;
	}

	public static boolean isIdentical(ItemStack lhs, ItemStack rhs) {
		if (lhs == null || rhs == null)
			return false;

		if (lhs.getItem() != rhs.getItem())
			return false;

		if (lhs.getItemDamage() != OreDictionary.WILDCARD_VALUE)
			if (lhs.getItemDamage() != rhs.getItemDamage())
				return false;

		return ItemStack.areItemStackTagsEqual(lhs, rhs);
	}

	public static List<ItemStack> getSubItems(Item item) {
		ArrayList<ItemStack> itemStacks = new ArrayList<ItemStack>();

		if (item == null)
			return itemStacks;

		ArrayList<ItemStack> subItems = new ArrayList<ItemStack>();
		for (CreativeTabs itemTab : item.getCreativeTabs()) {
			subItems.clear();
			item.getSubItems(item, itemTab, subItems);
			itemStacks.addAll(subItems);

			if (subItems.isEmpty()) {
				ItemStack stack = new ItemStack(item);
				if (stack.getItem() != null) {
					itemStacks.add(stack);
				}
			}
		}
		return removeDuplicateItemStacks(itemStacks);
	}

	public static List<ItemStack> getItemStacksRecursive(Iterable stacks) {
		ArrayList<ItemStack> itemStacks = new ArrayList<ItemStack>();
		for (Object obj : stacks) {
			if (obj	instanceof Iterable) {
				List<ItemStack> list2 = getItemStacksRecursive((Iterable) obj);
				itemStacks.addAll(list2);
			} else if (obj instanceof ItemStack) {
				ItemStack itemStack = (ItemStack)obj;
				if (itemStack.getItemDamage() == OreDictionary.WILDCARD_VALUE && itemStack.getHasSubtypes()) {
					Item item = itemStack.getItem();
					itemStacks.addAll(StackUtil.getSubItems(item));
				} else {
					itemStacks.add(itemStack);
				}
			} else if (obj != null) {
				Log.error("Unknown object found: " + obj);
				return null;
			}
		}
		return itemStacks;
	}

}
