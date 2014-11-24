package mezz.jei.util;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
	public static List<List<ItemStack>> getAllSubtypes(@Nonnull Iterable stacks) {
		List<List<ItemStack>> allSubtypes = new ArrayList<List<ItemStack>>();
		getAllSubtypes(allSubtypes, stacks);
		return allSubtypes;
	}

	private static void getAllSubtypes(List<List<ItemStack>> allSubtypes, Iterable stacks) {
		for (Object obj : stacks) {
			if (obj instanceof ItemStack) {
				ItemStack itemStack = (ItemStack) obj;
				List<ItemStack> subtypes = getSubtypes(itemStack);
				allSubtypes.add(subtypes);
			} else if (obj instanceof Iterable) {
				List<List<ItemStack>> objSubtypes = getAllSubtypes((Iterable)obj);
				List<ItemStack> subtypes = toItemStackList(objSubtypes);
				allSubtypes.add(subtypes);
			} else if (obj != null) {
				Log.error("Unknown object found: " + obj);
				allSubtypes.add(null);
			} else {
				allSubtypes.add(null);
			}
		}
	}

	public static Set<ItemStack> getAllSubtypesSet(Iterable stacks) {
		Set<ItemStack> allSubtypes = new HashSet<ItemStack>();
		getAllSubtypesSet(allSubtypes, stacks);
		return allSubtypes;
	}

	private static void getAllSubtypesSet(Set<ItemStack> subtypesSet, Iterable stacks) {
		for (Object obj : stacks) {
			if (obj instanceof ItemStack) {
				ItemStack itemStack = (ItemStack) obj;
				List<ItemStack> subtypes = getSubtypes(itemStack);
				subtypesSet.addAll(subtypes);
			} else if (obj instanceof Iterable) {
				getAllSubtypesSet(subtypesSet, (Iterable)obj);
			} else if (obj != null) {
				Log.error("Unknown object found: " + obj);
			}
		}
	}

	@Nonnull
	public static List<ItemStack> toItemStackList(@Nonnull Iterable stacks) {
		List<ItemStack> itemStacksList = new ArrayList<ItemStack>();
		toItemStackList(itemStacksList, stacks);
		return removeDuplicateItemStacks(itemStacksList);
	}

	private static void toItemStackList(@Nonnull List<ItemStack> itemStackList, @Nonnull Iterable input) {
		for (Object obj : input) {
			if (obj	instanceof Iterable) {
				toItemStackList(itemStackList, (Iterable) obj);
			} else if (obj instanceof ItemStack) {
				itemStackList.add((ItemStack) obj);
			} else if (obj != null) {
				Log.error("Unknown object found: " + obj);
			}
		}
	}

}
