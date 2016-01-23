package mezz.jei.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

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
import mezz.jei.api.recipe.IStackHelper;
import mezz.jei.gui.ingredients.IGuiIngredient;

public class StackHelper implements IStackHelper {
	/**
	 * Returns a list of items in slots that complete the recipe defined by requiredStacksList.
	 * Returns a result that contains missingItems if there are not enough items in availableItemStacks.
	 */
	@Nonnull
	public MatchingItemsResult getMatchingItems(@Nonnull List<ItemStack> availableItemStacks, @Nonnull Map<Integer, ? extends IGuiIngredient<ItemStack>> ingredientsMap) {
		MatchingItemsResult matchingItemResult = new MatchingItemsResult();

		int recipeSlotNumber = -1;
		SortedSet<Integer> keys = new TreeSet<>(ingredientsMap.keySet());
		for (Integer key : keys) {
			IGuiIngredient<ItemStack> ingredient = ingredientsMap.get(key);
			if (!ingredient.isInput()) {
				continue;
			}
			recipeSlotNumber++;

			List<ItemStack> requiredStacks = ingredient.getAll();
			if (requiredStacks.isEmpty()) {
				continue;
			}

			ItemStack matching = containsStack(availableItemStacks, requiredStacks);
			if (matching == null) {
				matchingItemResult.missingItems.add(key);
			} else {
				ItemStack matchingSplit = matching.splitStack(1);
				if (matching.stackSize == 0) {
					availableItemStacks.remove(matching);
				}
				matchingItemResult.matchingItems.put(recipeSlotNumber, matchingSplit);
			}
		}

		return matchingItemResult;
	}

	@Nullable
	public Slot getSlotWithStack(@Nonnull Container container, @Nonnull Iterable<Integer> slotNumbers, @Nonnull ItemStack stack) {
		StackHelper stackHelper = Internal.getStackHelper();

		for (Integer slotNumber : slotNumbers) {
			Slot slot = container.getSlot(slotNumber);
			if (slot != null) {
				ItemStack slotStack = slot.getStack();
				if (stackHelper.isIdentical(stack, slotStack)) {
					return slot;
				}
			}
		}
		return null;
	}

	@Nonnull
	public List<ItemStack> removeDuplicateItemStacks(@Nonnull Iterable<ItemStack> stacks) {
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
	public ItemStack containsStack(@Nullable Iterable<ItemStack> stacks, @Nullable Iterable<ItemStack> contains) {
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
	public ItemStack containsStack(@Nullable Iterable<ItemStack> stacks, @Nullable ItemStack contains) {
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

	public boolean isIdentical(@Nullable ItemStack lhs, @Nullable ItemStack rhs) {
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

	@Override
	@Nonnull
	public List<ItemStack> getSubtypes(@Nullable ItemStack itemStack) {
		if (itemStack == null) {
			Log.error("Null itemStack", new NullPointerException());
			return Collections.emptyList();
		}

		Item item = itemStack.getItem();
		if (item == null) {
			Log.error("Null item in itemStack", new NullPointerException());
			return Collections.emptyList();
		}

		if (itemStack.getItemDamage() != OreDictionary.WILDCARD_VALUE) {
			return Collections.singletonList(itemStack);
		}

		return getSubtypes(item, itemStack.stackSize);
	}

	@Nonnull
	public List<ItemStack> getSubtypes(@Nonnull Item item, int stackSize) {
		List<ItemStack> itemStacks = new ArrayList<>();

		for (CreativeTabs itemTab : item.getCreativeTabs()) {
			List<ItemStack> subItems = new ArrayList<>();
			item.getSubItems(item, itemTab, subItems);
			for (ItemStack subItem : subItems) {
				if (subItem.stackSize != stackSize) {
					ItemStack subItemCopy = subItem.copy();
					subItemCopy.stackSize = stackSize;
					itemStacks.add(subItemCopy);
				} else {
					itemStacks.add(subItem);
				}
			}
		}

		return itemStacks;
	}

	@Override
	@Nonnull
	public List<ItemStack> getAllSubtypes(@Nullable Iterable stacks) {
		if (stacks == null) {
			Log.error("Null stacks", new NullPointerException());
			return Collections.emptyList();
		}

		List<ItemStack> allSubtypes = new ArrayList<>();
		getAllSubtypes(allSubtypes, stacks);
		return allSubtypes;
	}

	private void getAllSubtypes(@Nonnull List<ItemStack> subtypesList, @Nonnull Iterable stacks) {
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

	@Override
	@Nonnull
	public List<ItemStack> toItemStackList(@Nullable Object stacks) {
		if (stacks == null) {
			return Collections.emptyList();
		}

		List<ItemStack> itemStacksList = new ArrayList<>();
		toItemStackList(itemStacksList, stacks);
		return removeDuplicateItemStacks(itemStacksList);
	}

	private void toItemStackList(@Nonnull List<ItemStack> itemStackList, @Nullable Object input) {
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
	public String getUniqueIdentifierForStack(@Nonnull ItemStack stack) {
		return getUniqueIdentifierForStack(stack, false);
	}

	@Nonnull
	public String getUniqueIdentifierForStack(@Nonnull ItemStack stack, boolean wildcard) {
		Item item = stack.getItem();
		if (item == null) {
			throw new ItemUidException("Found an itemStack with a null item. This is an error from another mod.");
		}

		FMLControlledNamespacedRegistry<Item> itemRegistry = GameData.getItemRegistry();
		ResourceLocation itemName = itemRegistry.getNameForObject(item);
		if (itemName == null) {
			throw new ItemUidException("No name for item in GameData itemRegistry: " + item.getClass());
		}

		String itemNameString = itemName.toString();
		int metadata = stack.getMetadata();
		if (wildcard || metadata == OreDictionary.WILDCARD_VALUE) {
			return itemNameString;
		}

		StringBuilder itemKey = new StringBuilder(itemNameString);
		if (stack.getHasSubtypes()) {
			itemKey.append(':').append(metadata);
			if (stack.hasTagCompound()) {
				NBTTagCompound nbtTagCompound = Internal.getHelpers().getNbtIgnoreList().getNbt(stack);
				if (nbtTagCompound != null && !nbtTagCompound.hasNoTags()) {
					itemKey.append(':').append(nbtTagCompound);
				}
			}
		}

		return itemKey.toString();
	}

	@Nonnull
	public List<String> getUniqueIdentifiersWithWildcard(@Nonnull ItemStack itemStack) {
		String uid = getUniqueIdentifierForStack(itemStack, false);
		String uidWild = getUniqueIdentifierForStack(itemStack, true);

		if (uid.equals(uidWild)) {
			return Collections.singletonList(uid);
		} else {
			return Arrays.asList(uid, uidWild);
		}
	}

	public int addStack(@Nonnull Container container, @Nonnull Collection<Integer> slotIndexes, @Nonnull ItemStack stack, boolean doAdd) {
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

	public static class MatchingItemsResult {
		@Nonnull
		public final Map<Integer, ItemStack> matchingItems = new HashMap<>();
		@Nonnull
		public final List<Integer> missingItems = new ArrayList<>();
	}
}
