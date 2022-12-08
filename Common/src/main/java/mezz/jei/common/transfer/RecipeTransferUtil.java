package mezz.jei.common.transfer;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IStackHelper;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferManager;
import mezz.jei.common.util.StringUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class RecipeTransferUtil {
	private static final Logger LOGGER = LogManager.getLogger();

	private RecipeTransferUtil() {
	}

	public static Optional<IRecipeTransferError> getTransferRecipeError(IRecipeTransferManager recipeTransferManager, AbstractContainerMenu container, IRecipeLayoutDrawable<?> recipeLayout, Player player) {
		return transferRecipe(recipeTransferManager, container, recipeLayout, player, false, false);
	}

	public static boolean transferRecipe(IRecipeTransferManager recipeTransferManager, AbstractContainerMenu container, IRecipeLayoutDrawable<?> recipeLayout, Player player, boolean maxTransfer) {
		return transferRecipe(recipeTransferManager, container, recipeLayout, player, maxTransfer, true)
			.map(error -> error.getType().allowsTransfer)
			.orElse(true);
	}

	private static <C extends AbstractContainerMenu, R> Optional<IRecipeTransferError> transferRecipe(
		IRecipeTransferManager recipeTransferManager,
		C container,
		IRecipeLayoutDrawable<R> recipeLayout,
		Player player,
		boolean maxTransfer,
		boolean doTransfer
	) {
		IRecipeCategory<R> recipeCategory = recipeLayout.getRecipeCategory();

		Optional<IRecipeTransferHandler<C, R>> recipeTransferHandler = recipeTransferManager.getRecipeTransferHandler(container, recipeCategory);
		if (recipeTransferHandler.isEmpty()) {
			if (doTransfer) {
				LOGGER.error("No Recipe Transfer handler for container {}", container.getClass());
			}
			return Optional.of(RecipeTransferErrorInternal.INSTANCE);
		}

		IRecipeTransferHandler<C, R> transferHandler = recipeTransferHandler.get();
		IRecipeSlotsView recipeSlotsView = recipeLayout.getRecipeSlotsView();

		try {
			IRecipeTransferError transferError = transferHandler.transferRecipe(container, recipeLayout.getRecipe(), recipeSlotsView, player, maxTransfer, doTransfer);
			return Optional.ofNullable(transferError);
		} catch (RuntimeException e) {
			LOGGER.error(
				"Recipe transfer handler '{}' for container '{}' and recipe type '{}' threw an error: ",
				transferHandler.getClass(), transferHandler.getContainerClass(), recipeCategory.getRecipeType().getUid(), e
			);
			return Optional.of(RecipeTransferErrorInternal.INSTANCE);
		}
	}

	public static boolean validateSlots(
		Player player,
		Collection<TransferOperation> transferOperations,
		Collection<Slot> craftingSlots,
		Collection<Slot> inventorySlots
	) {
		Set<Integer> inventorySlotIndexes = inventorySlots.stream()
			.map(s -> s.index)
			.collect(Collectors.toSet());
		Set<Integer> craftingSlotIndexes = craftingSlots.stream()
			.map(s -> s.index)
			.collect(Collectors.toSet());

		// check that all craftingTargetSlots are included in craftingSlots
		{
			List<Integer> invalidRecipeIndexes = transferOperations.stream()
				.map(TransferOperation::craftingSlot)
				.map(s -> s.index)
				.filter(s -> !craftingSlotIndexes.contains(s))
				.toList();
			if (!invalidRecipeIndexes.isEmpty()) {
				LOGGER.error(
					"Transfer handler has invalid slots for the destination of the recipe, " +
						"the slots are not included in the list of crafting slots. " +
						StringUtil.intsToString(invalidRecipeIndexes)
				);
				return false;
			}
		}

		// check that all ingredientTargetSlots are included in inventorySlots or recipeSlots
		{
			List<Integer> invalidInventorySlotIndexes = transferOperations.stream()
				.map(TransferOperation::inventorySlot)
				.map(s -> s.index)
				.filter(s -> !inventorySlotIndexes.contains(s) && !craftingSlotIndexes.contains(s))
				.toList();
			if (!invalidInventorySlotIndexes.isEmpty()) {
				LOGGER.error(
					"Transfer handler has invalid source slots for the inventory stacks for the recipe, " +
						"the slots are not included in the list of inventory slots or recipe slots. " +
						StringUtil.intsToString(invalidInventorySlotIndexes) +
						"\n inventory slots: " + StringUtil.intsToString(inventorySlotIndexes) +
						"\n crafting slots: " + StringUtil.intsToString(craftingSlotIndexes)
				);
				return false;
			}
		}

		// check that crafting slots and inventory slots do not overlap
		{
			Set<Integer> overlappingSlots = inventorySlotIndexes.stream()
				.filter(craftingSlotIndexes::contains)
				.collect(Collectors.toSet());
			if (!overlappingSlots.isEmpty()) {
				LOGGER.error(
					"Transfer handler has invalid slots, " +
						"inventorySlots and craftingSlots should not share any slot, but both have: " +
						StringUtil.intsToString(overlappingSlots)
				);
				return false;
			}
		}

		// check that all slots can be picked up by the player
		{
			List<Integer> invalidPickupSlots = Stream.concat(
					craftingSlots.stream(),
					inventorySlots.stream()
				)
				.filter(Slot::hasItem)
				.filter(slot -> !slot.mayPickup(player))
				.map(slot -> slot.index)
				.toList();
			if (!invalidPickupSlots.isEmpty()) {
				LOGGER.error(
					"Transfer handler has invalid slots, " +
						"the player is unable to pickup from them: " +
						StringUtil.intsToString(invalidPickupSlots)
				);
				return false;
			}
		}

		return true;
	}

	/**
	 * Returns a list of items in slots that complete the recipe defined by requiredStacksList.
	 * Returns a result that contains missingItems if there are not enough items in availableItemStacks.
	 */
	public static RecipeTransferOperationsResult getRecipeTransferOperations(
		IStackHelper stackhelper,
		Map<Slot, ItemStack> availableItemStacks,
		List<IRecipeSlotView> requiredItemStacks,
		List<Slot> craftingSlots
	) {
		RecipeTransferOperationsResult transferOperations = new RecipeTransferOperationsResult();

		// Find groups of slots for each recipe input, so each ingredient knows list of slots it can take item from
		// and also split them between "equal" groups
		Map<IRecipeSlotView, Map<ItemStack, ArrayList<PhantomSlotState>>> relevantSlots = new IdentityHashMap<>();

		for (Map.Entry<Slot, ItemStack> slotTuple : availableItemStacks.entrySet()) {
			for (IRecipeSlotView ingredient : requiredItemStacks) {
				if (!ingredient.isEmpty() && ingredient.getItemStacks().anyMatch(it -> stackhelper.isEquivalent(it, slotTuple.getValue(), UidContext.Ingredient))) {
					relevantSlots
						.computeIfAbsent(ingredient, it -> new Object2ObjectOpenCustomHashMap<>(new Hash.Strategy<>() {
							@Override
							public int hashCode(ItemStack o) {
								return o.getItem().hashCode();
							}

							@Override
							public boolean equals(ItemStack a, ItemStack b) {
								return stackhelper.isEquivalent(a, b, UidContext.Ingredient);
							}
						}))
						.computeIfAbsent(slotTuple.getValue(), it -> new ArrayList<>())
						.add(new PhantomSlotState(slotTuple.getKey(), slotTuple.getValue()));
				}
			}
		}

		// Now we have Ingredient -> (type -> slots) list
		// But it is not sorted
		// So we construct a List containing Ingredient -> List<Lists of slots>
		// Then we sort each List so children List of slots so that List with Slots which contain
		// the most items appear at top (this is outer sort)

		// After we have done outer sort, we need to do inner sort, that is, sort lists containing slots themselves
		// so that slots with lesser items appear at top

		// We need to get following structure:
		// Ingredient1 -> listOf(MostItems(LeastItemsInSlot, MoreItemsInSlot, ...), LesserItems(), ...)

		Map<IRecipeSlotView, ArrayList<PhantomSlotStateList>> bestMatches = new Object2ObjectArrayMap<>();

		for (Map.Entry<IRecipeSlotView, Map<ItemStack, ArrayList<PhantomSlotState>>> entry : relevantSlots.entrySet()) {
			ArrayList<PhantomSlotStateList> countedAndSorted = new ArrayList<>();

			for (Map.Entry<ItemStack, ArrayList<PhantomSlotState>> foundSlots : entry.getValue().entrySet()) {
				// Ascending sort
				// if counts are equal, push slots with lesser index to top
				foundSlots.getValue().sort((o1, o2) -> {
					int compare = Integer.compare(o1.itemStack.getCount(), o2.itemStack.getCount());

					if (compare == 0) {
						return Integer.compare(o1.slot.index, o2.slot.index);
					}

					return compare;
				});

				countedAndSorted.add(new PhantomSlotStateList(foundSlots.getValue()));
			}

			// Descending sort
			// if counts are equal, push groups with lowest slot index to top
			countedAndSorted.sort((o1, o2) -> {
				int compare = Long.compare(o2.totalItemCount, o1.totalItemCount);

				if (compare == 0) {
					return Integer.compare(
						o1.stateList.stream().mapToInt(it -> it.slot.index).min().orElse(0),
						o2.stateList.stream().mapToInt(it -> it.slot.index).min().orElse(0)
					);
				}

				return compare;
			});

			bestMatches.put(entry.getKey(), countedAndSorted);
		}

		// Fill in empty lists for missing ingredients, to simplify logic later
		for (IRecipeSlotView ingredient : requiredItemStacks) {
			if (!ingredient.isEmpty()) {
				bestMatches.computeIfAbsent(ingredient, it -> new ArrayList<>());
			}
		}

		for (int i = 0; i < requiredItemStacks.size(); i++) {
			IRecipeSlotView requiredItemStack = requiredItemStacks.get(i);

			if (requiredItemStack.isEmpty()) {
				continue;
			}

			Slot craftingSlot = craftingSlots.get(i);

			PhantomSlotState matching = bestMatches
				.get(requiredItemStack)
				.stream()
				.flatMap(PhantomSlotStateList::stream)
				.findFirst().orElse(null);

			if (matching == null) {
				transferOperations.missingItems.add(requiredItemStack);
			} else {
				Slot matchingSlot = matching.slot;
				ItemStack matchingStack = matching.itemStack;
				matchingStack.shrink(1);
				transferOperations.results.add(new TransferOperation(matchingSlot, craftingSlot));
			}
		}

		return transferOperations;
	}

	private record PhantomSlotState(Slot slot, ItemStack itemStack) {}

	private record PhantomSlotStateList(List<PhantomSlotState> stateList, long totalItemCount) {
		public PhantomSlotStateList(List<PhantomSlotState> states) {
			this(states, states.stream().mapToLong(it -> it.itemStack.getCount()).sum());
		}

		public Stream<PhantomSlotState> stream() {
			return this.stateList.stream().filter(it -> !it.itemStack.isEmpty());
		}
	}
}
