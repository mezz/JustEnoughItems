package mezz.jei.transfer;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import mezz.jei.Internal;
import mezz.jei.util.StackHelper;
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
		StackHelper stackHelper = Internal.getStackHelper();

		// grab items from slots
		Map<Integer, ItemStack> slotMap = new HashMap<Integer, ItemStack>(slotIdMap.size());
		for (Map.Entry<Integer, Integer> entry : slotIdMap.entrySet()) {
			Slot slot = container.getSlot(entry.getValue());
			final ItemStack slotStack = slot.getStack();
			if (slotStack == null) {
				return;
			}
			ItemStack stack = slotStack.copy();
			stack.stackSize = 1;
			slotMap.put(entry.getKey(), stack);
		}

		// remove required recipe items
		int removedSets = removeSetsFromInventory(container, slotMap.values(), craftingSlots, inventorySlots, maxTransfer);
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
			if (stack.isStackable()) {
				int maxStackSize = Math.min(slot.getItemStackLimit(stack), stack.getMaxStackSize());
				int maxSets = maxStackSize / stack.stackSize;
				stack.stackSize *= Math.min(maxSets, removedSets);
			}

			slot.putStack(stack);
		}

		// put cleared items back into the player's inventory
		for (ItemStack oldCraftingItem : clearedCraftingItems) {
			stackHelper.addStack(container, inventorySlots, oldCraftingItem, true);
		}

		container.detectAndSendChanges();
	}

	private static int removeSetsFromInventory(Container container, Collection<ItemStack> required, List<Integer> craftingSlots, List<Integer> inventorySlots, boolean maxTransfer) {
		if (maxTransfer) {
			List<ItemStack> requiredCopy = new ArrayList<ItemStack>();
			requiredCopy.addAll(required);

			int removedSets = 0;
			while (!requiredCopy.isEmpty() && removeSetsFromInventory(container, requiredCopy, craftingSlots, inventorySlots)) {
				removedSets++;
				Iterator<ItemStack> iterator = requiredCopy.iterator();
				while (iterator.hasNext()) {
					ItemStack stack = iterator.next();
					if (!stack.isStackable() || (stack.stackSize * (removedSets + 1) > stack.getMaxStackSize())) {
						iterator.remove();
					}
				}
			}
			return removedSets;
		} else {
			boolean success = removeSetsFromInventory(container, required, craftingSlots, inventorySlots);
			return success ? 1 : 0;
		}
	}

	private static boolean removeSetsFromInventory(Container container, Iterable<ItemStack> required, List<Integer> craftingSlots, List<Integer> inventorySlots) {
		final Map<Slot, ItemStack> originalSlotContents = new HashMap<Slot, ItemStack>();

		for (ItemStack matchingStack : required) {
			final ItemStack requiredStack = matchingStack.copy();
			while (requiredStack.stackSize > 0) {
				final Slot slot = getSlotWithStack(container, requiredStack, craftingSlots, inventorySlots);
				if (slot == null || slot.getStack() == null) {
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

				ItemStack removed = slot.decrStackSize(requiredStack.stackSize);
				requiredStack.stackSize -= removed.stackSize;
			}
		}

		return true;
	}

	@Nullable
	private static Slot getSlotWithStack(Container container, ItemStack stack, List<Integer> craftingSlots, List<Integer> inventorySlots) {
		StackHelper stackHelper = Internal.getStackHelper();

		Slot slot = stackHelper.getSlotWithStack(container, craftingSlots, stack);
		if (slot == null) {
			slot = stackHelper.getSlotWithStack(container, inventorySlots, stack);
		}

		return slot;
	}
}
