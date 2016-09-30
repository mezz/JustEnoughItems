package mezz.jei.util;

import javax.annotation.Nullable;
import java.util.Collection;

import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class InventoryHelper {
	public static int addStack(Container container, Collection<Integer> slotIndexes, ItemStack stack, boolean doAdd) {
		int added = 0;
		// Add to existing stacks first
		for (final Integer slotIndex : slotIndexes) {
			if (slotIndex >= 0 && slotIndex < container.inventorySlots.size()) {
				final Slot slot = container.getSlot(slotIndex);
				final ItemStack inventoryStack = slot.getStack();
				// Check that the slot's contents are stackable with this stack
				if (inventoryStack != null &&
						inventoryStack.getItem() != null &&
						inventoryStack.isStackable() &&
						inventoryStack.isItemEqual(stack) &&
						ItemStack.areItemStackTagsEqual(inventoryStack, stack)) {

					final int remain = stack.stackSize - added;
					final int maxStackSize = Math.min(slot.getItemStackLimit(inventoryStack), inventoryStack.getMaxStackSize());
					final int space = maxStackSize - inventoryStack.stackSize;
					if (space > 0) {

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
				}
			}
		}

		if (added >= stack.stackSize) {
			return added;
		}

		for (final Integer slotIndex : slotIndexes) {
			if (slotIndex >= 0 && slotIndex < container.inventorySlots.size()) {
				final Slot slot = container.getSlot(slotIndex);
				final ItemStack inventoryStack = slot.getStack();
				if (inventoryStack == null) {
					if (doAdd) {
						ItemStack stackToAdd = stack.copy();
						stackToAdd.stackSize = stack.stackSize - added;
						slot.putStack(stackToAdd);
					}
					return stack.stackSize;
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
	public static Slot getSlotWithStack(Container container, Iterable<Integer> slotNumbers, ItemStack itemStack) {
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
