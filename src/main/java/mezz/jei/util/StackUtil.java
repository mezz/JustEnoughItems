package mezz.jei.util;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StackUtil {

	@Nonnull
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
	@Nullable
	public static ItemStack containsStack(@Nullable Iterable<ItemStack> stacks, @Nullable ItemStack contains) {
		if (stacks == null || contains == null)
			return null;

		for (ItemStack stack : stacks) {
			if (isIdentical(stack, contains))
				return stack;
		}
		return null;
	}

	public static boolean isIdentical(@Nullable ItemStack lhs, @Nullable ItemStack rhs) {
		if (lhs == rhs)
			return true;

		if (lhs == null || rhs == null)
			return false;

		if (lhs.getItem() != rhs.getItem())
			return false;

		if (lhs.getItemDamage() != OreDictionary.WILDCARD_VALUE)
			if (lhs.getItemDamage() != rhs.getItemDamage())
				return false;

		return ItemStack.areItemStackTagsEqual(lhs, rhs);
	}

	/**
	 * Returns all the subtypes of itemStack if it has a wildcard meta value.
	 */
	@Nonnull
	public static List<ItemStack> getSubtypes(@Nonnull ItemStack itemStack) {

		ArrayList<ItemStack> itemStacks = new ArrayList<ItemStack>();

		Item item = itemStack.getItem();
		if (item == null)
			return itemStacks;

		if (!item.getHasSubtypes() || item.getDamage(itemStack) != OreDictionary.WILDCARD_VALUE)
			return Collections.singletonList(itemStack);

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

	@Nonnull
	public static List<ItemStack> getAllSubtypes(@Nonnull Iterable stacks) {
		ArrayList<ItemStack> itemStacks = new ArrayList<ItemStack>();
		for (Object obj : stacks) {
			if (obj instanceof ItemStack) {
				ItemStack itemStack = (ItemStack) obj;
				itemStacks.addAll(StackUtil.getSubtypes(itemStack));
			}
		}
		return removeDuplicateItemStacks(itemStacks);
	}

	@Nonnull
	public static List<ItemStack> getItemStacksRecursive(@Nonnull Iterable stacks) {
		ArrayList<ItemStack> itemStacks = new ArrayList<ItemStack>();
		for (Object obj : stacks) {
			if (obj	instanceof Iterable) {
				List<ItemStack> list2 = getItemStacksRecursive((Iterable) obj);
				itemStacks.addAll(list2);
			} else if (obj instanceof ItemStack) {
				itemStacks.add((ItemStack)obj);
			} else if (obj != null) {
				Log.error("Unknown object found: " + obj);
			}
		}
		return removeDuplicateItemStacks(itemStacks);
	}

}
