package mezz.jei.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import net.minecraftforge.fml.common.registry.GameData;
import net.minecraftforge.oredict.OreDictionary;

public class StackUtil {

	@Nonnull
	public static List<ItemStack> removeDuplicateItemStacks(Iterable<ItemStack> stacks) {
		List<ItemStack> newStacks = new ArrayList<>();
		if (stacks == null) {
			return newStacks;
		}

		for (ItemStack stack : stacks) {
			if (stack != null && containsStack(newStacks, stack) == null) {
				newStacks.add(stack);
			}
		}
		return newStacks;
	}

	/* Returns an ItemStack from "stacks" if it isIdentical to "contains" */
	@Nullable
	public static ItemStack containsStack(@Nullable Iterable<ItemStack> stacks, @Nullable ItemStack contains) {
		if (stacks == null || contains == null) {
			return null;
		}

		for (ItemStack stack : stacks) {
			if (isIdentical(stack, contains)) {
				return stack;
			}
		}
		return null;
	}

	public static boolean isIdentical(@Nullable ItemStack lhs, @Nullable ItemStack rhs) {
		if (lhs == rhs) {
			return true;
		}

		if (lhs == null || rhs == null) {
			return false;
		}

		if (lhs.getItem() != rhs.getItem()) {
			return false;
		}

		if (lhs.getItemDamage() != OreDictionary.WILDCARD_VALUE) {
			if (lhs.getItemDamage() != rhs.getItemDamage()) {
				return false;
			}
		}

		return ItemStack.areItemStackTagsEqual(lhs, rhs);
	}

	/**
	 * Returns all the subtypes of itemStack if it has a wildcard meta value.
	 */
	@Nonnull
	public static List<ItemStack> getSubtypes(@Nonnull ItemStack itemStack) {

		List<ItemStack> itemStacks = new ArrayList<>();

		Item item = itemStack.getItem();
		if (item == null) {
			return itemStacks;
		}

		if (item.getDamage(itemStack) != OreDictionary.WILDCARD_VALUE) {
			return Collections.singletonList(itemStack);
		}

		if (!item.getHasSubtypes()) {
			return Collections.singletonList(new ItemStack(item));
		}

		List<ItemStack> subItems = new ArrayList<>();
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

	public static List<ItemStack> getAllSubtypes(Iterable stacks) {
		List<ItemStack> allSubtypes = new ArrayList<>();
		getAllSubtypes(allSubtypes, stacks);
		return allSubtypes;
	}

	private static void getAllSubtypes(List<ItemStack> subtypesList, Iterable stacks) {
		for (Object obj : stacks) {
			if (obj instanceof ItemStack) {
				ItemStack itemStack = (ItemStack) obj;
				List<ItemStack> subtypes = getSubtypes(itemStack);
				subtypesList.addAll(subtypes);
			} else if (obj instanceof Iterable) {
				getAllSubtypes(subtypesList, (Iterable) obj);
			} else if (obj != null) {
				Log.error("Unknown object found: %s", obj);
			}
		}
	}

	@Nonnull
	public static List<ItemStack> toItemStackList(@Nullable Iterable stacks) {
		if (stacks == null) {
			return Collections.emptyList();
		}
		List<ItemStack> itemStacksList = new ArrayList<>();
		toItemStackList(itemStacksList, stacks);
		return removeDuplicateItemStacks(itemStacksList);
	}

	private static void toItemStackList(@Nonnull List<ItemStack> itemStackList, @Nonnull Iterable input) {
		for (Object obj : input) {
			if (obj instanceof Iterable) {
				toItemStackList(itemStackList, (Iterable) obj);
			} else if (obj instanceof ItemStack) {
				itemStackList.add((ItemStack) obj);
			} else if (obj != null) {
				Log.error("Unknown object found: %s", obj);
			}
		}
	}

	@Nonnull
	public static String uniqueIdentifierForStack(@Nonnull ItemStack stack, boolean wildcard) {
		Item item = stack.getItem();
		Object itemName = GameData.getItemRegistry().getNameForObject(item);
		String itemNameString = String.valueOf(itemName);
		if (wildcard) {
			return itemNameString;
		}

		StringBuilder itemKey = new StringBuilder(itemNameString).append(':').append(stack.getItemDamage());
		if (stack.hasTagCompound()) {
			itemKey.append(':').append(stack.getTagCompound());
		}

		return itemKey.toString();
	}
}
