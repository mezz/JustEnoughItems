package mezz.jei.transfer;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class BasicRecipeTransferHandlerServer {
	private static final Logger LOGGER = LogManager.getLogger();

	private BasicRecipeTransferHandlerServer() {
	}

	/**
	 * Called server-side to actually put the items in place.
	 */
	public static void setItems(
		Player player,
		List<TransferOperation> transferOperations,
		List<Slot> craftingSlots,
		List<Slot> inventorySlots,
		boolean maxTransfer,
		boolean requireCompleteSets
	) {
		if (!RecipeTransferUtil.validateSlots(player, transferOperations, craftingSlots, inventorySlots)) {
			return;
		}

		Map<Slot, ItemStack> recipeSlotToRequiredItemStack = calculateRequiredStacks(transferOperations, player);
		if (recipeSlotToRequiredItemStack == null) {
			return;
		}

		// Transfer as many items as possible only if it has been explicitly requested by the implementation
		// and a max-transfer operation has been requested by the player.
		boolean transferAsCompleteSets = requireCompleteSets || !maxTransfer;

		Map<Slot, ItemStack> recipeSlotToTakenStacks = takeItemsFromInventory(
			player,
			recipeSlotToRequiredItemStack,
			craftingSlots,
			inventorySlots,
			transferAsCompleteSets,
			maxTransfer
		);

		if (recipeSlotToTakenStacks.isEmpty()) {
			LOGGER.error("Tried to transfer recipe but was unable to remove any items from the inventory.");
			return;
		}

		// clear the crafting grid
		List<ItemStack> clearedCraftingItems = clearCraftingGrid(craftingSlots, player);

		// put items into the crafting grid
		List<ItemStack> remainderItems = putItemsIntoCraftingGrid(recipeSlotToTakenStacks, requireCompleteSets);

		// put leftover items back into the inventory
		stowItems(player, inventorySlots, clearedCraftingItems);
		stowItems(player, inventorySlots, remainderItems);

		AbstractContainerMenu container = player.containerMenu;
		container.broadcastChanges();
	}

	private static int getSlotStackLimit(
		Map<Slot, ItemStack> recipeSlotToTakenStacks,
		boolean requireCompleteSets
	) {
		if (!requireCompleteSets) {
			return Integer.MAX_VALUE;
		}

		return recipeSlotToTakenStacks.entrySet().stream()
			.mapToInt(e -> {
				Slot craftingSlot = e.getKey();
				ItemStack transferItem = e.getValue();
				if (craftingSlot.mayPlace(transferItem)) {
					return craftingSlot.getMaxStackSize(transferItem);
				}
				return Integer.MAX_VALUE;
			})
			.min()
			.orElse(Integer.MAX_VALUE);
	}

	private static List<ItemStack> clearCraftingGrid(List<Slot> craftingSlots, Player player) {
		List<ItemStack> clearedCraftingItems = new ArrayList<>();
		for (Slot craftingSlot : craftingSlots) {
			if (!craftingSlot.mayPickup(player)) {
				continue;
			}
			if (craftingSlot.hasItem()) {
				ItemStack craftingItem = craftingSlot.remove(Integer.MAX_VALUE);
				clearedCraftingItems.add(craftingItem);
			}
		}
		return clearedCraftingItems;
	}

	private static List<ItemStack> putItemsIntoCraftingGrid(
		Map<Slot, ItemStack> recipeSlotToTakenStacks,
		boolean requireCompleteSets
	) {
		final int slotStackLimit = getSlotStackLimit(recipeSlotToTakenStacks, requireCompleteSets);
		List<ItemStack> remainderItems = new ArrayList<>();

		recipeSlotToTakenStacks.forEach((slot, stack) -> {
			if (slot.getItem().isEmpty() && slot.mayPlace(stack)) {
				ItemStack remainder = slot.safeInsert(stack, slotStackLimit);
				if (!remainder.isEmpty()) {
					remainderItems.add(remainder);
				}
			} else {
				remainderItems.add(stack);
			}
		});

		return remainderItems;
	}

	@Nullable
	private static Map<Slot, ItemStack> calculateRequiredStacks(List<TransferOperation> transferOperations, Player player) {
		Map<Slot, ItemStack> recipeSlotToRequired = new HashMap<>(transferOperations.size());
		for (TransferOperation transferOperation : transferOperations) {
			Slot recipeSlot = transferOperation.craftingSlot();
			Slot inventorySlot = transferOperation.inventorySlot();
			if (!inventorySlot.mayPickup(player)) {
				LOGGER.error(
					"Tried to transfer recipe but was given an" +
					" inventory slot that the player can't pickup from: {}" ,
					inventorySlot.index
				);
				return null;
			}
			final ItemStack slotStack = inventorySlot.getItem();
			if (slotStack.isEmpty()) {
				LOGGER.error(
					"Tried to transfer recipe but was given an" +
					" empty inventory slot as an ingredient source: {}",
					inventorySlot.index
				);
				return null;
			}
			ItemStack stack = slotStack.copy();
			stack.setCount(1);
			recipeSlotToRequired.put(recipeSlot, stack);
		}
		return recipeSlotToRequired;
	}

	@Nonnull
	private static Map<Slot, ItemStack> takeItemsFromInventory(
		Player player,
		Map<Slot, ItemStack> recipeSlotToRequiredItemStack,
		List<Slot> craftingSlots,
		List<Slot> inventorySlots,
		boolean transferAsCompleteSets,
		boolean maxTransfer
	) {
		if (!maxTransfer) {
			return removeOneSetOfItemsFromInventory(
				player,
				recipeSlotToRequiredItemStack,
				craftingSlots,
				inventorySlots,
				transferAsCompleteSets
			);
		}

		final Map<Slot, ItemStack> recipeSlotToResult = new HashMap<>(recipeSlotToRequiredItemStack.size());
		while (true) {
			final Map<Slot, ItemStack> foundItemsInSet = removeOneSetOfItemsFromInventory(
				player,
				recipeSlotToRequiredItemStack,
				craftingSlots,
				inventorySlots,
				transferAsCompleteSets
			);

			if (foundItemsInSet.isEmpty()) {
				break;
			}

			// Merge the contents of the temporary map with the result map.
			Set<Slot> fullSlots = merge(recipeSlotToResult, foundItemsInSet);

			// to avoid overfilling slots, remove any requirements that have been met
			for (Slot fullSlot : fullSlots) {
				recipeSlotToRequiredItemStack.remove(fullSlot);
			}
		}

		return recipeSlotToResult;
	}

	private static Map<Slot, ItemStack> removeOneSetOfItemsFromInventory(
		Player player,
		Map<Slot, ItemStack> recipeSlotToRequiredItemStack,
		List<Slot> craftingSlots,
		List<Slot> inventorySlots,
		boolean transferAsCompleteSets
	) {
		Map<Slot, ItemStack> originalSlotContents = null;
		if (transferAsCompleteSets) {
			// We only need to create a new map for each set iteration if we're transferring as complete sets.
			originalSlotContents = new HashMap<>();
		}

		// This map holds items found for each set iteration. Its contents are added to the result map
		// after each complete set iteration. If we are transferring as complete sets, this allows
		// us to simply ignore the map's contents when a complete set isn't found.
		final Map<Slot, ItemStack> foundItemsInSet = new HashMap<>(recipeSlotToRequiredItemStack.size());

		for (Map.Entry<Slot, ItemStack> entry : recipeSlotToRequiredItemStack.entrySet()) { // for each item in set
			final Slot recipeSlot = entry.getKey();
			final ItemStack requiredStack = entry.getValue();

			// Locate a slot that has what we need.
			final Slot slot = getSlotWithStack(player, requiredStack, craftingSlots, inventorySlots);
			if (slot != null) {
				// the item was found

				// Keep a copy of the slot's original contents in case we need to roll back.
				if (originalSlotContents != null && !originalSlotContents.containsKey(slot)) {
					originalSlotContents.put(slot, slot.getItem().copy());
				}

				// Reduce the size of the found slot.
				ItemStack removedItemStack = slot.remove(1);
				foundItemsInSet.put(recipeSlot, removedItemStack);
			} else {
				// We can't find any more slots to fulfill the requirements.

				if (transferAsCompleteSets) {
					// Since the full set requirement wasn't satisfied, we need to roll back any
					// slot changes we've made during this set iteration.
					for (Map.Entry<Slot, ItemStack> slotEntry : originalSlotContents.entrySet()) {
						ItemStack stack = slotEntry.getValue();
						slotEntry.getKey().set(stack);
					}
					return Map.of();
				}
			}
		}
		return foundItemsInSet;
	}

	private static Set<Slot> merge(Map<Slot, ItemStack> result, Map<Slot, ItemStack> addition) {
		Set<Slot> fullSlots = new HashSet<>();

		addition.forEach((slot, itemStack) -> {
			assert itemStack.getCount() == 1;

			ItemStack resultItemStack = result.get(slot);
			if (resultItemStack == null) {
				resultItemStack = itemStack;
				result.put(slot, resultItemStack);
			} else {
				assert ItemStack.isSameItemSameTags(resultItemStack, itemStack);
				resultItemStack.grow(itemStack.getCount());
			}
			if (resultItemStack.getCount() == slot.getMaxStackSize(resultItemStack)) {
				fullSlots.add(slot);
			}
		});

		return fullSlots;
	}

	@Nullable
	private static Slot getSlotWithStack(Player player, ItemStack stack, List<Slot> craftingSlots, List<Slot> inventorySlots) {
		Slot slot = getSlotWithStack(player, craftingSlots, stack);
		if (slot == null) {
			slot = getSlotWithStack(player, inventorySlots, stack);
		}

		return slot;
	}

	private static void stowItems(Player player, List<Slot> inventorySlots, List<ItemStack> itemStacks) {
		for (ItemStack itemStack : itemStacks) {
			ItemStack remainder = stowItem(inventorySlots, itemStack);
			if (!remainder.isEmpty()) {
				if (!player.getInventory().add(remainder)) {
					player.drop(remainder, false);
				}
			}
		}
	}

	private static ItemStack stowItem(Collection<Slot> slots, ItemStack stack) {
		if (stack.isEmpty()) {
			return ItemStack.EMPTY;
		}

		final ItemStack remainder = stack.copy();

		// Add to existing stacks first
		for (Slot slot : slots) {
			final ItemStack inventoryStack = slot.getItem();
			if (!inventoryStack.isEmpty() && inventoryStack.isStackable()) {
				slot.safeInsert(remainder);
				if (remainder.isEmpty()) {
					return ItemStack.EMPTY;
				}
			}
		}

		// Try adding to empty slots
		for (Slot slot : slots) {
			if (slot.getItem().isEmpty()) {
				slot.safeInsert(remainder);
				if (remainder.isEmpty()) {
					return ItemStack.EMPTY;
				}
			}
		}

		return remainder;
	}

	/**
	 * Get the slot which contains a specific itemStack.
	 *
	 * @param slots     the slots in the container to search
	 * @param itemStack the itemStack to find
	 * @return the slot that contains the itemStack. returns null if no slot contains the itemStack.
	 */
	@Nullable
	private static Slot getSlotWithStack(Player player, Iterable<Slot> slots, ItemStack itemStack) {
		for (Slot slot : slots) {
			ItemStack slotStack = slot.getItem();
			if (ItemStack.isSameItemSameTags(itemStack, slotStack) &&
				slot.mayPickup(player)
			) {
				return slot;
			}
		}
		return null;
	}
}
