package mezz.jei.common.transfer;

import mezz.jei.common.platform.IPlatformTransactionHelper.ITransaction;
import mezz.jei.common.platform.Services;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
		setItemsWithResult(player, transferOperations, craftingSlots, inventorySlots, maxTransfer, requireCompleteSets);
	}

	public static boolean setItemsFromItemHandlersWithResult(
		Player player,
		List<RecipeTransferRequirement> requirements,
		List<Slot> craftingSlots,
		List<Slot> inventorySlots,
		boolean maxTransfer,
		boolean requireCompleteSets
	) {
		try {
			if (!RecipeTransferUtil.validateSlots(player, List.of(), craftingSlots, inventorySlots) ||
				!validateRequirements(requirements, craftingSlots)
			) {
				return false;
			}

			Map<RecipeTransferSource, ItemStack> availableItemStacks = getAvailableItemStacks(
				player,
				craftingSlots,
				inventorySlots
			);
			List<TransferOperation> transferOperations = RecipeTransferUtil.getExactRecipeTransferOperations(
				availableItemStacks,
				requirements,
				craftingSlots
			);
			if (transferOperations == null) {
				return false;
			}

			return setItemsWithResult(
				player,
				transferOperations,
				craftingSlots,
				inventorySlots,
				maxTransfer,
				requireCompleteSets,
				true
			);
		} catch (RuntimeException e) {
			LOGGER.error("Failed to plan recipe transfer from item handlers", e);
			return false;
		}
	}

	/**
	 * Called server-side to put the items in place and report whether the transfer was applied.
	 */
	public static boolean setItemsWithResult(
		Player player,
		List<TransferOperation> transferOperations,
		List<Slot> craftingSlots,
		List<Slot> inventorySlots,
		boolean maxTransfer,
		boolean requireCompleteSets
	) {
		return setItemsWithResult(
			player,
			transferOperations,
			craftingSlots,
			inventorySlots,
			maxTransfer,
			requireCompleteSets,
			false
		);
	}

	private static boolean setItemsWithResult(
		Player player,
		List<TransferOperation> transferOperations,
		List<Slot> craftingSlots,
		List<Slot> inventorySlots,
		boolean maxTransfer,
		boolean requireCompleteSets,
		boolean allowItemHandlerFallback
	) {
		if (!RecipeTransferUtil.validateSlots(player, transferOperations, craftingSlots, inventorySlots)) {
			return false;
		}
		if (!canClearCraftingSlots(player, craftingSlots)) {
			return false;
		}

		List<RequiredTransfer> requiredTransfers = calculateRequiredTransfers(transferOperations, player);
		if (requiredTransfers == null) {
			return false;
		}

		// Transfer as many items as possible only if it has been explicitly requested by the implementation
		// and a max-transfer operation has been requested by the player.
		boolean transferAsCompleteSets = requireCompleteSets || !maxTransfer;

		try (ITransaction transaction = Services.PLATFORM.getTransactionHelper().openTransaction()) {
			Map<Slot, ItemStack> recipeSlotToTakenStacks = takeItemsFromInventory(
				transaction,
				player,
				requiredTransfers,
				craftingSlots,
				inventorySlots,
				transferAsCompleteSets,
				maxTransfer,
				allowItemHandlerFallback
			);

			if (recipeSlotToTakenStacks.isEmpty()) {
				LOGGER.error("Tried to transfer recipe but was unable to remove any items from the inventory.");
				return false;
			}

			// clear the crafting grid
			List<ItemStack> clearedCraftingItems = clearCraftingGrid(transaction, craftingSlots, player);
			if (clearedCraftingItems == null) {
				LOGGER.error("Tried to transfer recipe but was unable to clear the crafting grid.");
				return false;
			}

			// put items into the crafting grid
			List<ItemStack> remainderItems = putItemsIntoCraftingGrid(
				transaction,
				recipeSlotToTakenStacks,
				requireCompleteSets
			);
			if (remainderItems == null) {
				LOGGER.error("Tried to transfer recipe but was unable to put items into the crafting grid.");
				return false;
			}

			// put leftover items back into the inventory before committing the transaction
			List<Slot> stowSlots = getStowSlots(player, inventorySlots);
			if (!stowItems(transaction, player, stowSlots, clearedCraftingItems) ||
				!stowItems(transaction, player, stowSlots, remainderItems)
			) {
				LOGGER.error("Tried to transfer recipe but was unable to stow leftover items.");
				return false;
			}
			transaction.commit();
		} catch (RuntimeException e) {
			LOGGER.error("Failed to apply recipe transfer transaction", e);
			return false;
		}

		AbstractContainerMenu container = player.containerMenu;
		container.broadcastChanges();
		return true;
	}

	private static boolean validateRequirements(
		List<RecipeTransferRequirement> requirements,
		List<Slot> craftingSlots
	) {
		if (requirements.isEmpty() ||
			requirements.size() > RecipeTransferRequirement.MAX_REQUIREMENTS ||
			requirements.size() > craftingSlots.size()
		) {
			return false;
		}

		Set<Integer> craftingSlotIndexes = craftingSlots.stream()
			.map(slot -> slot.index)
			.collect(Collectors.toSet());
		Set<Integer> seenSlotIndexes = new HashSet<>();
		int totalAlternatives = 0;
		for (RecipeTransferRequirement requirement : requirements) {
			int alternativeCount = requirement.acceptedStacks().size();
			if (!craftingSlotIndexes.contains(requirement.craftingSlotId()) ||
				!seenSlotIndexes.add(requirement.craftingSlotId()) ||
				alternativeCount == 0 ||
				alternativeCount > RecipeTransferRequirement.MAX_ALTERNATIVES ||
				requirement.acceptedStacks().stream().anyMatch(ItemStack::isEmpty)
			) {
				return false;
			}
			totalAlternatives += alternativeCount;
			if (totalAlternatives > RecipeTransferRequirement.MAX_TOTAL_ALTERNATIVES) {
				return false;
			}
		}
		return true;
	}

	private static Map<RecipeTransferSource, ItemStack> getAvailableItemStacks(
		Player player,
		List<Slot> craftingSlots,
		List<Slot> inventorySlots
	) {
		Map<RecipeTransferSource, ItemStack> availableItemStacks = new HashMap<>();
		for (Slot craftingSlot : craftingSlots) {
			ItemStack stack = craftingSlot.getItem();
			if (!stack.isEmpty() && craftingSlot.allowModification(player)) {
				availableItemStacks.put(new RecipeTransferSource(craftingSlot), stack.copy());
			}
		}

		for (Slot inventorySlot : inventorySlots) {
			ItemStack stack = inventorySlot.getItem();
			if (stack.isEmpty() || !inventorySlot.allowModification(player)) {
				continue;
			}

			Optional<List<ItemStack>> itemHandlerContents = Services.PLATFORM.getRecipeTransferHelper()
				.getItemHandlerContents(stack);
			if (itemHandlerContents.isEmpty()) {
				availableItemStacks.put(new RecipeTransferSource(inventorySlot), stack.copy());
				continue;
			}

			List<ItemStack> contents = itemHandlerContents.get();
			for (int itemHandlerSlotId = 0; itemHandlerSlotId < contents.size(); itemHandlerSlotId++) {
				ItemStack content = contents.get(itemHandlerSlotId);
				if (!content.isEmpty()) {
					availableItemStacks.put(
						new RecipeTransferSource(inventorySlot, itemHandlerSlotId),
						content
					);
				}
			}
		}
		return availableItemStacks;
	}

	private static boolean canClearCraftingSlots(Player player, List<Slot> craftingSlots) {
		for (Slot craftingSlot : craftingSlots) {
			ItemStack stack = craftingSlot.getItem();
			if (!stack.isEmpty() && (!craftingSlot.mayPickup(player) || !craftingSlot.mayPlace(stack))) {
				LOGGER.error(
					"Tried to transfer recipe but crafting slot {} contains an item that cannot be moved: {}",
					craftingSlot.index,
					stack
				);
				return false;
			}
		}
		return true;
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

	@Nullable
	private static List<ItemStack> clearCraftingGrid(
		ITransaction transaction,
		List<Slot> craftingSlots,
		Player player
	) {
		List<ItemStack> clearedCraftingItems = new ArrayList<>();
		for (Slot craftingSlot : craftingSlots) {
			if (!craftingSlot.mayPickup(player)) {
				continue;
			}

			ItemStack item = craftingSlot.getItem();
			if (!item.isEmpty() && craftingSlot.mayPlace(item)) {
				Optional<ItemStack> craftingItem = extractExactFromSlot(transaction, craftingSlot, item.getCount(), player);
				if (craftingItem.isEmpty()) {
					return null;
				}
				clearedCraftingItems.add(craftingItem.get());
			}
		}
		return clearedCraftingItems;
	}

	@Nullable
	private static List<ItemStack> putItemsIntoCraftingGrid(
		ITransaction transaction,
		Map<Slot, ItemStack> recipeSlotToTakenStacks,
		boolean requireCompleteSets
	) {
		final int slotStackLimit = getSlotStackLimit(recipeSlotToTakenStacks, requireCompleteSets);
		List<ItemStack> remainderItems = new ArrayList<>();

		for (Map.Entry<Slot, ItemStack> entry : recipeSlotToTakenStacks.entrySet()) {
			Slot slot = entry.getKey();
			ItemStack stack = entry.getValue();
			ItemStack remainder = transaction.insertIntoSlot(slot, stack, slotStackLimit);
			int insertedCount = stack.getCount() - remainder.getCount();
			if (insertedCount <= 0) {
				return null;
			}
			if (!remainder.isEmpty()) {
				remainderItems.add(remainder);
			}
		}

		return remainderItems;
	}

	@Nullable
	private static List<RequiredTransfer> calculateRequiredTransfers(List<TransferOperation> transferOperations, Player player) {
		List<RequiredTransfer> requiredTransfers = new ArrayList<>(transferOperations.size());
		Map<Slot, ItemStack> targetSlotStacks = new HashMap<>();
		Map<Slot, Boolean> itemHandlerSourceModes = new HashMap<>();
		for (TransferOperation transferOperation : transferOperations) {
			Slot recipeSlot = transferOperation.craftingSlot(player.containerMenu);
			Slot inventorySlot = transferOperation.inventorySlot(player.containerMenu);
			if (!inventorySlot.allowModification(player)) {
				LOGGER.error(
					"Tried to transfer recipe but was given an inventory slot that the player can't pickup from: {}",
					inventorySlot.index
				);
				return null;
			}
			boolean itemHandlerSource = transferOperation.hasItemHandlerSource();
			Boolean previousSourceMode = itemHandlerSourceModes.putIfAbsent(inventorySlot, itemHandlerSource);
			if (previousSourceMode != null && previousSourceMode != itemHandlerSource) {
				LOGGER.error(
					"Tried to use inventory slot {} as both a direct and item handler recipe source",
					inventorySlot.index
				);
				return null;
			}

			RecipeTransferSource source = new RecipeTransferSource(inventorySlot, transferOperation.itemHandlerSlotId());
			final ItemStack sourceStack = getSourceStack(source);
			if (sourceStack.isEmpty() || sourceStack.getCount() < transferOperation.count()) {
				LOGGER.error(
					"Tried to transfer recipe but was given an empty or insufficient ingredient source: {}",
					source
				);
				return null;
			}
			ItemStack stack = sourceStack.copy();
			stack.setCount(transferOperation.count());
			if (!recipeSlot.mayPlace(stack)) {
				LOGGER.error(
					"Tried to transfer recipe but crafting slot {} does not accept ingredient: {}",
					recipeSlot.index,
					stack
				);
				return null;
			}
			ItemStack targetSlotStack = targetSlotStacks.putIfAbsent(recipeSlot, stack);
			if (targetSlotStack != null && !ItemStack.isSameItemSameComponents(targetSlotStack, stack)) {
				LOGGER.error(
					"Tried to transfer different ingredients into the same crafting slot {}: {} and {}",
					recipeSlot.index,
					targetSlotStack,
					stack
				);
				return null;
			}
			requiredTransfers.add(new RequiredTransfer(recipeSlot, source, stack));
		}
		return requiredTransfers;
	}

	private static Map<Slot, ItemStack> takeItemsFromInventory(
		ITransaction transaction,
		Player player,
		List<RequiredTransfer> requiredTransfers,
		List<Slot> craftingSlots,
		List<Slot> inventorySlots,
		boolean transferAsCompleteSets,
		boolean maxTransfer,
		boolean allowItemHandlerFallback
	) {
		if (!maxTransfer) {
			return removeOneSetOfItemsFromInventory(
				transaction,
				player,
				requiredTransfers,
				craftingSlots,
				inventorySlots,
				transferAsCompleteSets,
				allowItemHandlerFallback
			);
		}

		List<RequiredTransfer> remainingRequiredTransfers = new ArrayList<>(requiredTransfers);
		final Map<Slot, ItemStack> recipeSlotToResult = new HashMap<>(requiredTransfers.size());
		while (true) {
			removeFullRecipeSlots(remainingRequiredTransfers, recipeSlotToResult);
			if (remainingRequiredTransfers.isEmpty()) {
				break;
			}

			final Map<Slot, ItemStack> foundItemsInSet = removeOneSetOfItemsFromInventory(
				transaction,
				player,
				remainingRequiredTransfers,
				craftingSlots,
				inventorySlots,
				transferAsCompleteSets,
				allowItemHandlerFallback
			);

			if (foundItemsInSet.isEmpty()) {
				break;
			}

			// Merge the contents of the temporary map with the result map.
			merge(recipeSlotToResult, foundItemsInSet);
		}

		return recipeSlotToResult;
	}

	private static void removeFullRecipeSlots(List<RequiredTransfer> requiredTransfers, Map<Slot, ItemStack> recipeSlotToResult) {
		Set<Slot> fullRecipeSlots = new HashSet<>();
		for (RequiredTransfer requiredTransfer : requiredTransfers) {
			Slot recipeSlot = requiredTransfer.recipeSlot;
			ItemStack resultStack = recipeSlotToResult.get(recipeSlot);
			if (resultStack == null) {
				continue;
			}
			int requiredCount = getRequiredCount(requiredTransfers, recipeSlot);
			int maxStackSize = Integer.MAX_VALUE;
			if (recipeSlot.mayPlace(resultStack)) {
				maxStackSize = recipeSlot.getMaxStackSize(resultStack);
			}
			if (resultStack.getCount() + requiredCount > maxStackSize) {
				fullRecipeSlots.add(recipeSlot);
			}
		}
		requiredTransfers.removeIf(requiredTransfer -> fullRecipeSlots.contains(requiredTransfer.recipeSlot));
	}

	private static int getRequiredCount(List<RequiredTransfer> requiredTransfers, Slot recipeSlot) {
		return requiredTransfers.stream()
			.filter(requiredTransfer -> requiredTransfer.recipeSlot == recipeSlot)
			.mapToInt(requiredTransfer -> requiredTransfer.stack.getCount())
			.sum();
	}

	private static Map<Slot, ItemStack> removeOneSetOfItemsFromInventory(
		ITransaction transaction,
		Player player,
		List<RequiredTransfer> requiredTransfers,
		List<Slot> craftingSlots,
		List<Slot> inventorySlots,
		boolean transferAsCompleteSets,
		boolean allowItemHandlerFallback
	) {
		// This map holds items found for each set iteration. Its contents are added to the result map
		// after each complete set iteration. If we are transferring as complete sets, this allows
		// us to simply ignore the map's contents when a complete set isn't found.
		final Map<Slot, ItemStack> foundItemsInSet = new HashMap<>(requiredTransfers.size());
		ItemHandlerSources itemHandlerSources = ItemHandlerSources.EMPTY;
		if (allowItemHandlerFallback) {
			itemHandlerSources = getItemHandlerSources(player, inventorySlots);
		}
		ITransaction activeTransaction = transaction;
		if (transferAsCompleteSets) {
			activeTransaction = transaction.openNested();
		}
		try {
			for (RequiredTransfer requiredTransfer : requiredTransfers) {
				final Slot recipeSlot = requiredTransfer.recipeSlot;
				final ItemStack requiredStack = requiredTransfer.stack;
				final RecipeTransferSource hint = requiredTransfer.hint;

				RecipeTransferSource source = getSourceWithStack(player, requiredStack, craftingSlots, inventorySlots, hint, itemHandlerSources)
					.orElse(null);
				if (source != null) {
					Optional<ItemStack> removedItemStack = extractExactFromSource(
						activeTransaction,
						player,
						source,
						requiredStack
					);
					if (removedItemStack.isPresent()) {
						merge(foundItemsInSet, recipeSlot, removedItemStack.get());
						continue;
					}
				}

				if (transferAsCompleteSets) {
					return Map.of();
				}
			}

			if (transferAsCompleteSets) {
				activeTransaction.commit();
			}
			return foundItemsInSet;
		} finally {
			if (transferAsCompleteSets) {
				activeTransaction.close();
			}
		}
	}

	private static Optional<ItemStack> extractExactFromSource(
		ITransaction transaction,
		Player player,
		RecipeTransferSource source,
		ItemStack requiredStack
	) {
		try (ITransaction nestedTransaction = transaction.openNested()) {
			ItemStack extracted;
			if (source.isItemHandlerSource()) {
				extracted = nestedTransaction.extractFromItemHandler(
					source.slot().getItem(),
					source.itemHandlerSlotId(),
					requiredStack
				);
			} else {
				extracted = nestedTransaction.extractFromSlot(source.slot(), requiredStack.getCount(), player);
			}

			if (extracted.getCount() != requiredStack.getCount() ||
				!ItemStack.isSameItemSameComponents(extracted, requiredStack)
			) {
				return Optional.empty();
			}
			nestedTransaction.commit();
			return Optional.of(extracted);
		}
	}

	private static Optional<ItemStack> extractExactFromSlot(
		ITransaction transaction,
		Slot slot,
		int count,
		Player player
	) {
		try (ITransaction nestedTransaction = transaction.openNested()) {
			ItemStack extracted = nestedTransaction.extractFromSlot(slot, count, player);
			if (extracted.getCount() != count) {
				return Optional.empty();
			}
			nestedTransaction.commit();
			return Optional.of(extracted);
		}
	}

	private static void merge(Map<Slot, ItemStack> result, Map<Slot, ItemStack> addition) {
		addition.forEach((slot, itemStack) -> {
			merge(result, slot, itemStack);
		});
	}

	private static ItemStack merge(Map<Slot, ItemStack> result, Slot slot, ItemStack itemStack) {
		ItemStack resultItemStack = result.get(slot);
		if (resultItemStack == null) {
			resultItemStack = itemStack;
			result.put(slot, resultItemStack);
		} else {
			assert ItemStack.isSameItemSameComponents(resultItemStack, itemStack);
			resultItemStack.grow(itemStack.getCount());
		}
		return resultItemStack;
	}

	private static Optional<RecipeTransferSource> getSourceWithStack(
		Player player,
		ItemStack stack,
		List<Slot> craftingSlots,
		List<Slot> inventorySlots,
		RecipeTransferSource hint,
		ItemHandlerSources itemHandlerSources
	) {
		return getValidatedHintSource(player, stack, hint)
			.or(() -> getSlotWithStack(player, craftingSlots, stack, Set.of()))
			.or(() -> getSlotWithStack(player, inventorySlots, stack, itemHandlerSources.slots()))
			.or(() -> getItemHandlerSourceWithStack(player, itemHandlerSources.sources(), stack));
	}

	private static ItemHandlerSources getItemHandlerSources(Player player, List<Slot> inventorySlots) {
		List<RecipeTransferSource> sources = new ArrayList<>();
		Set<Slot> slots = new HashSet<>();
		for (Slot inventorySlot : inventorySlots) {
			ItemStack stack = inventorySlot.getItem();
			if (stack.isEmpty() || !inventorySlot.allowModification(player)) {
				continue;
			}

			Optional<List<ItemStack>> itemHandlerContents = Services.PLATFORM.getRecipeTransferHelper()
				.getItemHandlerContents(stack);
			if (itemHandlerContents.isEmpty()) {
				continue;
			}
			slots.add(inventorySlot);
			List<ItemStack> contents = itemHandlerContents.get();
			for (int itemHandlerSlotId = 0; itemHandlerSlotId < contents.size(); itemHandlerSlotId++) {
				if (!contents.get(itemHandlerSlotId).isEmpty()) {
					sources.add(new RecipeTransferSource(inventorySlot, itemHandlerSlotId));
				}
			}
		}
		return new ItemHandlerSources(sources, slots);
	}

	private static Optional<RecipeTransferSource> getItemHandlerSourceWithStack(
		Player player,
		List<RecipeTransferSource> sources,
		ItemStack stack
	) {
		return sources.stream()
			.filter(source -> isValidAndMatches(player, source, stack))
			.findFirst();
	}

	private static Optional<RecipeTransferSource> getValidatedHintSource(Player player, ItemStack stack, RecipeTransferSource hint) {
		if (isValidAndMatches(player, hint, stack)) {
			return Optional.of(hint);
		}

		return Optional.empty();
	}

	private static List<Slot> getStowSlots(Player player, List<Slot> inventorySlots) {
		List<Slot> stowSlots = new ArrayList<>(inventorySlots);
		for (Slot playerInventorySlot : getPlayerInventorySlots(player)) {
			if (!stowSlots.contains(playerInventorySlot)) {
				stowSlots.add(playerInventorySlot);
			}
		}
		return stowSlots;
	}

	private static List<Slot> getPlayerInventorySlots(Player player) {
		AbstractContainerMenu container = player.containerMenu;
		return container.slots.stream()
			.filter(slot -> slot.container == player.getInventory())
			.filter(slot -> slot.getContainerSlot() < Inventory.INVENTORY_SIZE)
			.toList();
	}

	private static boolean stowItems(
		ITransaction transaction,
		Player player,
		List<Slot> inventorySlots,
		List<ItemStack> itemStacks
	) {
		for (ItemStack itemStack : itemStacks) {
			if (!stowItem(transaction, player, inventorySlots, itemStack)) {
				return false;
			}
		}
		return true;
	}

	private static boolean stowItem(
		ITransaction transaction,
		Player player,
		Collection<Slot> slots,
		ItemStack stack
	) {
		if (stack.isEmpty()) {
			return true;
		}

		try (ITransaction nestedTransaction = transaction.openNested()) {
			ItemStack remainder = insertIntoSlots(nestedTransaction, player, slots, stack);
			if (!remainder.isEmpty()) {
				return false;
			}
			nestedTransaction.commit();
			return true;
		}
	}

	private static ItemStack insertIntoSlots(
		ITransaction transaction,
		Player player,
		Collection<Slot> slots,
		ItemStack stack
	) {
		ItemStack remainder = stack.copy();

		// Add to existing stacks first
		for (Slot slot : slots) {
			if (!slot.mayPickup(player)) {
				continue;
			}
			final ItemStack inventoryStack = slot.getItem();
			if (!inventoryStack.isEmpty() &&
				inventoryStack.isStackable() &&
				ItemStack.isSameItemSameComponents(inventoryStack, remainder)
			) {
				remainder = transaction.insertIntoSlot(slot, remainder, remainder.getCount());
				if (remainder.isEmpty()) {
					return ItemStack.EMPTY;
				}
			}
		}

		// Try adding to empty slots
		for (Slot slot : slots) {
			if (slot.getItem().isEmpty()) {
				remainder = transaction.insertIntoSlot(slot, remainder, remainder.getCount());
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
	private static Optional<RecipeTransferSource> getSlotWithStack(Player player, Collection<Slot> slots, ItemStack itemStack, Set<Slot> excludedSlots) {
		return slots.stream()
			.filter(slot -> !excludedSlots.contains(slot))
			.map(RecipeTransferSource::new)
			.filter(source -> isValidAndMatches(player, source, itemStack))
			.findFirst();
	}

	private static boolean isValidAndMatches(Player player, RecipeTransferSource source, ItemStack stack) {
		ItemStack containedStack = getSourceStack(source);
		return ItemStack.isSameItemSameComponents(stack, containedStack) &&
			containedStack.getCount() >= stack.getCount() &&
			source.slot().allowModification(player);
	}

	private static ItemStack getSourceStack(RecipeTransferSource source) {
		if (!source.isItemHandlerSource()) {
			return source.slot().getItem();
		}

		Optional<List<ItemStack>> itemHandlerContents = Services.PLATFORM.getRecipeTransferHelper()
			.getItemHandlerContents(source.slot().getItem());
		if (itemHandlerContents.isEmpty()) {
			return ItemStack.EMPTY;
		}
		List<ItemStack> contents = itemHandlerContents.get();
		int itemHandlerSlotId = source.itemHandlerSlotId();
		if (itemHandlerSlotId < 0 || itemHandlerSlotId >= contents.size()) {
			return ItemStack.EMPTY;
		}
		return contents.get(itemHandlerSlotId);
	}

	private record RequiredTransfer(Slot recipeSlot, RecipeTransferSource hint, ItemStack stack) {}

	private record ItemHandlerSources(List<RecipeTransferSource> sources, Set<Slot> slots) {
		private static final ItemHandlerSources EMPTY = new ItemHandlerSources(List.of(), Set.of());
	}
}
