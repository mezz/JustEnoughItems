package mezz.jei.transfer;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mezz.jei.util.InventoryHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class BasicRecipeTransferHandlerServer {
	/**
	 * Called server-side to actually put the items in place.
	 */
	public static void setItems(EntityPlayer player, Map<Integer, Integer> slotIdMap, List<Integer> craftingSlots, List<Integer> inventorySlots, boolean maxTransfer) {
		Container container = player.openContainer;

		// grab items from slots
		Map<Integer, ItemStack> slotMap = new HashMap<Integer, ItemStack>(slotIdMap.size());
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
		List<ItemStack> clearedCraftingItems = new ArrayList<ItemStack>();
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
			int added = InventoryHelper.addStack(container, inventorySlots, oldCraftingItem, true);
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
		final Map<Slot, ItemStack> originalSlotContents = new HashMap<Slot, ItemStack>();

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
		Slot slot = InventoryHelper.getSlotWithStack(container, craftingSlots, stack);
		if (slot == null) {
			slot = InventoryHelper.getSlotWithStack(container, inventorySlots, stack);
		}

		return slot;
	}
}
