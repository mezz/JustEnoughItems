package mezz.jei.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.common.registry.FMLControlledNamespacedRegistry;
import net.minecraftforge.fml.common.registry.GameData;
import net.minecraftforge.oredict.OreDictionary;

import mezz.jei.Internal;

public class StackUtil {
	private StackUtil() {

	}

	@Nonnull
	public static List<ItemStack> removeDuplicateItemStacks(@Nonnull Iterable<ItemStack> stacks) {
		List<ItemStack> newStacks = new ArrayList<>();
		for (ItemStack stack : stacks) {
			if (stack != null && containsStack(newStacks, stack) == null) {
				newStacks.add(stack);
			}
		}
		return newStacks;
	}

	/* Returns an ItemStack from "stacks" if it isIdentical to an ItemStack from "contains" */
	@Nullable
	public static ItemStack containsStack(@Nullable Iterable<ItemStack> stacks, @Nullable Iterable<ItemStack> contains) {
		if (stacks == null || contains == null) {
			return null;
		}

		for (ItemStack containStack : contains) {
			ItemStack matchingStack = containsStack(stacks, containStack);
			if (matchingStack != null) {
				return matchingStack;
			}
		}

		return null;
	}

	/* Returns an ItemStack from "stacks" if it isIdentical to "contains" */
	@Nullable
	public static ItemStack containsStack(@Nullable Iterable<ItemStack> stacks, @Nullable ItemStack contains) {
		if (stacks == null || contains == null) {
			return null;
		}

		for (ItemStack stack : stacks) {
			if (isIdentical(contains, stack)) {
				return stack;
			}
		}
		return null;
	}

	@Nonnull
	public static List<ItemStack> condenseStacks(Collection<ItemStack> stacks) {
		List<ItemStack> condensed = new ArrayList<>();

		for (ItemStack stack : stacks) {
			if (stack == null || stack.stackSize <= 0) {
				continue;
			}

			boolean matched = false;
			for (ItemStack cached : condensed) {
				if (cached.isItemEqual(stack) && ItemStack.areItemStackTagsEqual(cached, stack)) {
					cached.stackSize += stack.stackSize;
					matched = true;
				}
			}

			if (!matched) {
				ItemStack cached = stack.copy();
				condensed.add(cached);
			}
		}

		return condensed;
	}

	/**
	 * Counts how many full sets are contained in the passed stock.
	 * Returns a list of matching stacks from set, or null if there aren't enough for a complete match.
	 */
	@Nullable
	public static List<ItemStack> containsSets(Collection<ItemStack> required, Collection<ItemStack> offered) {
		int totalSets = 0;

		List<ItemStack> matching = new ArrayList<>();
		List<ItemStack> condensedRequired = condenseStacks(required);
		List<ItemStack> condensedOffered = condenseStacks(offered);

		for (ItemStack req : condensedRequired) {
			int reqCount = 0;
			for (ItemStack offer : condensedOffered) {
				if (isIdentical(req, offer)) {
					int stackCount = offer.stackSize / req.stackSize;
					reqCount = Math.max(reqCount, stackCount);
				}
			}

			if (reqCount == 0) {
				return null;
			} else {
				matching.add(req);

				if (totalSets == 0 || totalSets > reqCount) {
					totalSets = reqCount;
				}
			}
		}

		return matching;
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

		if (lhs.getMetadata() != OreDictionary.WILDCARD_VALUE) {
			if (lhs.getMetadata() != rhs.getMetadata()) {
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
		Item item = itemStack.getItem();
		if (item == null) {
			return Collections.emptyList();
		}

		if (item.getMetadata(itemStack) != OreDictionary.WILDCARD_VALUE) {
			return Collections.singletonList(itemStack);
		}

		List<ItemStack> itemStacks = new ArrayList<>();

		for (CreativeTabs itemTab : item.getCreativeTabs()) {
			List<ItemStack> subItems = new ArrayList<>();
			item.getSubItems(item, itemTab, subItems);
			for (ItemStack subItem : subItems) {
				if (subItem.stackSize != itemStack.stackSize) {
					ItemStack subItemCopy = subItem.copy();
					subItemCopy.stackSize = itemStack.stackSize;
					itemStacks.add(subItemCopy);
				} else {
					itemStacks.add(subItem);
				}
			}
		}

		if (itemStacks.isEmpty()) {
			itemStacks.add(itemStack);
		}

		return itemStacks;
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
				Log.error("Unknown object found: {}", obj);
			}
		}
	}

	@Nonnull
	public static List<ItemStack> toItemStackList(@Nullable Object stacks) {
		List<ItemStack> itemStacksList = new ArrayList<>();
		toItemStackList(itemStacksList, stacks);
		return removeDuplicateItemStacks(itemStacksList);
	}

	private static void toItemStackList(@Nonnull List<ItemStack> itemStackList, @Nullable Object input) {
		if (input instanceof ItemStack) {
			itemStackList.add((ItemStack) input);
		} else if (input instanceof String) {
			List<ItemStack> stacks = OreDictionary.getOres((String) input);
			itemStackList.addAll(stacks);
		} else if (input instanceof Iterable) {
			for (Object obj : (Iterable) input) {
				toItemStackList(itemStackList, obj);
			}
		} else if (input != null) {
			Log.error("Unknown object found: {}", input);
		}
	}

	@Nonnull
	public static String getUniqueIdentifierForStack(@Nonnull ItemStack stack) {
		return getUniqueIdentifierForStack(stack, false);
	}

	@Nonnull
	public static String getUniqueIdentifierForStack(@Nonnull ItemStack stack, boolean wildcard) {
		Item item = stack.getItem();
		if (item == null) {
			throw new NullPointerException("Found an itemStack with a null item. This is an error from another mod.");
		}

		FMLControlledNamespacedRegistry<Item> itemRegistry = GameData.getItemRegistry();
		ResourceLocation itemName = itemRegistry.getNameForObject(item);
		if (itemName == null) {
			throw new NullPointerException("No name for item in GameData itemRegistry: " + item.getClass());
		}

		String itemNameString = itemName.toString();
		int metadata = stack.getMetadata();
		if (wildcard || metadata == OreDictionary.WILDCARD_VALUE) {
			return itemNameString;
		}

		StringBuilder itemKey = new StringBuilder(itemNameString).append(':').append(metadata);
		if (stack.hasTagCompound()) {
			NBTTagCompound nbtTagCompound = Internal.getHelpers().getNbtIgnoreList().getNbt(stack);
			if (nbtTagCompound != null && !nbtTagCompound.hasNoTags()) {
				itemKey.append(':').append(nbtTagCompound);
			}
		}

		return itemKey.toString();
	}

	@Nonnull
	public static List<String> getUniqueIdentifiersWithWildcard(@Nonnull ItemStack itemStack) {
		return Arrays.asList(
				getUniqueIdentifierForStack(itemStack, false),
				getUniqueIdentifierForStack(itemStack, true)
		);
	}

	public static int addStack(Container container, Collection<Integer> slotIndexes, ItemStack stack, boolean doAdd) {
		int added = 0;
		// Add to existing stacks first
		for (Integer slotIndex : slotIndexes) {
			Slot slot = container.getSlot(slotIndex);
			if (slot == null) {
				continue;
			}

			ItemStack inventoryStack = slot.getStack();
			if (inventoryStack == null || inventoryStack.getItem() == null) {
				continue;
			}

			// Already occupied by different item, skip this slot.
			if (!inventoryStack.isStackable() || !inventoryStack.isItemEqual(stack) || !ItemStack.areItemStackTagsEqual(inventoryStack, stack)) {
				continue;
			}

			int remain = stack.stackSize - added;
			int space = inventoryStack.getMaxStackSize() - inventoryStack.stackSize;
			if (space <= 0) {
				continue;
			}

			// Enough space
			if (space >= remain) {
				if (doAdd) {
					inventoryStack.stackSize += remain;
				}
				return stack.stackSize;
			}

			// Not enough space
			if (doAdd) {
				inventoryStack.stackSize = inventoryStack.getMaxStackSize();
			}

			added += space;
		}

		if (added >= stack.stackSize) {
			return added;
		}

		for (Integer slotIndex : slotIndexes) {
			Slot slot = container.getSlot(slotIndex);
			if (slot == null) {
				continue;
			}

			ItemStack inventoryStack = slot.getStack();
			if (inventoryStack != null) {
				continue;
			}

			if (doAdd) {
				ItemStack stackToAdd = stack.copy();
				stackToAdd.stackSize = stack.stackSize - added;
				slot.putStack(stackToAdd);
			}
			return stack.stackSize;
		}

		return added;
	}
}
