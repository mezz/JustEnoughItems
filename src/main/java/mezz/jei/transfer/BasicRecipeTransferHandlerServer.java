package mezz.jei.transfer;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public final class BasicRecipeTransferHandlerServer {
	private BasicRecipeTransferHandlerServer() {
	}

	/**
	 * Called server-side to actually put the items in place.
	 */
	public static void setItems(EntityPlayer player, Map<Integer, Integer> slotIdMap, List<Integer> craftingSlots, List<Integer> inventorySlots, boolean maxTransfer) {
		Container container = player.openContainer;

		// grab items from slots
		Map<Integer, ItemStack> slotMap = new HashMap<>(slotIdMap.size());
		for (Map.Entry<Integer, Integer> entry : slotIdMap.entrySet()) {
			Slot slot = container.getSlot(entry.getValue());
			final ItemStack slotStack = slot.getStack();
			if (slotStack.isEmpty()) {
				return;
			}
			ItemStack stack = slotStack.copy();
			stack.setCount(1);
			slotMap.put(entry.getKey(), stack);
		}

		int maxRemovedSets = maxTransfer ? 64 : 1;
		for (Map.Entry<Integer, ItemStack> entry : slotMap.entrySet()) {
			ItemStack stack = entry.getValue();
			if (stack.isStackable()) {
				Integer craftNumber = entry.getKey();
				Integer slotNumber = craftingSlots.get(craftNumber);
				Slot craftSlot = container.getSlot(slotNumber);
				int maxStackSize = Math.min(craftSlot.getItemStackLimit(stack), stack.getMaxStackSize());
				maxRemovedSets = Math.min(maxRemovedSets, maxStackSize);
			} else {
				maxRemovedSets = 1;
			}
		}

		if (maxRemovedSets <= 0) {
			return;
		}

		// remove required recipe items
		int removedSets = removeSetsFromInventory(container, slotMap.values(), craftingSlots, inventorySlots, maxRemovedSets);
		if (removedSets == 0) {
			return;
		}

		// clear the crafting grid
		List<ItemStack> clearedCraftingItems = new ArrayList<>();
		for (Integer craftingSlotNumber : craftingSlots) {
			Slot craftingSlot = container.getSlot(craftingSlotNumber);
			if (craftingSlot.getHasStack()) {
				ItemStack craftingItem = craftingSlot.decrStackSize(Integer.MAX_VALUE);
				clearedCraftingItems.add(craftingItem);
			}
		}

		// put items into the crafting grid
		for (Map.Entry<Integer, ItemStack> entry : slotMap.entrySet()) {
			Integer craftNumber = entry.getKey();
			Integer slotNumber = craftingSlots.get(craftNumber);
			Slot slot = container.getSlot(slotNumber);

			ItemStack stack = entry.getValue();
			stack.setCount(stack.getCount() * removedSets);
			if (slot.isItemValid(stack)) {
				slot.putStack(stack);
			} else {
				clearedCraftingItems.add(stack);
			}
		}

		// put cleared items back into the inventory
		for (ItemStack oldCraftingItem : clearedCraftingItems) {
			int added = addStack(container, inventorySlots, oldCraftingItem);
			if (added < oldCraftingItem.getCount()) {
				if (!player.inventory.addItemStackToInventory(oldCraftingItem)) {
					player.dropItem(oldCraftingItem, false);
				}
			}
		}

		container.detectAndSendChanges();
	}

	private static int removeSetsFromInventory(Container container, Collection<ItemStack> required, List<Integer> craftingSlots, List<Integer> inventorySlots, final int maxRemovedSets) {
		int removedSets = 0;
		while (removedSets < maxRemovedSets && removeSetsFromInventory(container, required, craftingSlots, inventorySlots)) {
			removedSets++;
		}
		return removedSets;
	}

	private static boolean removeSetsFromInventory(Container container, Iterable<ItemStack> required, List<Integer> craftingSlots, List<Integer> inventorySlots) {
		final Map<Slot, ItemStack> originalSlotContents = new HashMap<>();

		for (ItemStack matchingStack : required) {
			final ItemStack requiredStack = matchingStack.copy();
			while (requiredStack.getCount() > 0) {
				final Slot slot = getSlotWithStack(container, requiredStack, craftingSlots, inventorySlots);
				if (slot == null || slot.getStack().isEmpty()) {
					// abort! put removed items back where they came from
					for (Map.Entry<Slot, ItemStack> slotEntry : originalSlotContents.entrySet()) {
						ItemStack stack = slotEntry.getValue();
						slotEntry.getKey().putStack(stack);
					}
					return false;
				}

				if (!originalSlotContents.containsKey(slot)) {
					originalSlotContents.put(slot, slot.getStack().copy());
				}

				ItemStack removed = slot.decrStackSize(requiredStack.getCount());
				requiredStack.shrink(removed.getCount());
			}
		}

		return true;
	}

	@Nullable
	private static Slot getSlotWithStack(Container container, ItemStack stack, List<Integer> craftingSlots, List<Integer> inventorySlots) {
		Slot slot = getSlotWithStack(container, craftingSlots, stack);
		if (slot == null) {
			slot = getSlotWithStack(container, inventorySlots, stack);
		}

		return slot;
	}

	private static int addStack(Container container, Collection<Integer> slotIndexes, ItemStack stack) {
		int added = 0;
		// Add to existing stacks first
		for (final Integer slotIndex : slotIndexes) {
			if (slotIndex >= 0 && slotIndex < container.inventorySlots.size()) {
				final Slot slot = container.getSlot(slotIndex);
				final ItemStack inventoryStack = slot.getStack();
				// Check that the slot's contents are stackable with this stack
				if (!inventoryStack.isEmpty() &&
						inventoryStack.isStackable() &&
						inventoryStack.isItemEqual(stack) &&
						ItemStack.areItemStackTagsEqual(inventoryStack, stack)) {

					final int remain = stack.getCount() - added;
					final int maxStackSize = Math.min(slot.getItemStackLimit(inventoryStack), inventoryStack.getMaxStackSize());
					final int space = maxStackSize - inventoryStack.getCount();
					if (space > 0) {

						// Enough space
						if (space >= remain) {
							inventoryStack.grow(remain);
							return stack.getCount();
						}

						// Not enough space
						inventoryStack.setCount(inventoryStack.getMaxStackSize());

						added += space;
					}
				}
			}
		}

		if (added >= stack.getCount()) {
			return added;
		}

		for (final Integer slotIndex : slotIndexes) {
			if (slotIndex >= 0 && slotIndex < container.inventorySlots.size()) {
				final Slot slot = container.getSlot(slotIndex);
				final ItemStack inventoryStack = slot.getStack();
				if (inventoryStack.isEmpty()) {
					ItemStack stackToAdd = stack.copy();
					stackToAdd.setCount(stack.getCount() - added);
					slot.putStack(stackToAdd);
					return stack.getCount();
				}
			}
		}

		return added;
	}

	/**
	 * Get the slot which contains a specific itemStack.
	 *
	 * @param container   the container to search
	 * @param slotNumbers the slots in the container to search
	 * @param itemStack   the itemStack to find
	 * @return the slot that contains the itemStack. returns null if no slot contains the itemStack.
	 */
	@Nullable
	private static Slot getSlotWithStack(Container container, Iterable<Integer> slotNumbers, ItemStack itemStack) {
		for (Integer slotNumber : slotNumbers) {
			if (slotNumber >= 0 && slotNumber < container.inventorySlots.size()) {
				Slot slot = container.getSlot(slotNumber);
				ItemStack slotStack = slot.getStack();
				if (ItemStack.areItemsEqual(itemStack, slotStack) && ItemStack.areItemStackTagsEqual(itemStack, slotStack)) {
					return slot;
				}
			}
		}
		return null;
	}
}
