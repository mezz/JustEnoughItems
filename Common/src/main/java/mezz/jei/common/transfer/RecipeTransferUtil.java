package mezz.jei.common.transfer;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IStackHelper;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.ingredients.RecipeSlots;
import mezz.jei.common.gui.recipes.layout.IRecipeLayoutInternal;
import mezz.jei.common.recipes.RecipeTransferManager;
import mezz.jei.common.util.ItemStackMatchable;
import mezz.jei.common.util.MatchingIterable;
import mezz.jei.common.util.StringUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class RecipeTransferUtil {
	private static final Logger LOGGER = LogManager.getLogger();

	private RecipeTransferUtil() {
	}

	@Nullable
	public static IRecipeTransferError getTransferRecipeError(RecipeTransferManager recipeTransferManager, AbstractContainerMenu container, IRecipeLayoutInternal<?> recipeLayout, Player player) {
		return transferRecipe(recipeTransferManager, container, recipeLayout, player, false, false);
	}

	public static boolean transferRecipe(RecipeTransferManager recipeTransferManager, AbstractContainerMenu container, IRecipeLayoutInternal<?> recipeLayout, Player player, boolean maxTransfer) {
		IRecipeTransferError error = transferRecipe(recipeTransferManager, container, recipeLayout, player, maxTransfer, true);
		return allowsTransfer(error);
	}

	@Nullable
	private static <C extends AbstractContainerMenu, R> IRecipeTransferError transferRecipe(
		RecipeTransferManager recipeTransferManager,
		C container,
		IRecipeLayoutInternal<R> recipeLayout,
		Player player,
		boolean maxTransfer,
		boolean doTransfer
	) {
		if (Internal.getRuntime().isEmpty()) {
			return RecipeTransferErrorInternal.INSTANCE;
		}

		IRecipeCategory<R> recipeCategory = recipeLayout.getRecipeCategory();
		final IRecipeTransferHandler<C, R> transferHandler = recipeTransferManager.getRecipeTransferHandler(container, recipeCategory);
		if (transferHandler == null) {
			if (doTransfer) {
				LOGGER.error("No Recipe Transfer handler for container {}", container.getClass());
			}
			return RecipeTransferErrorInternal.INSTANCE;
		}

		RecipeSlots recipeSlots = recipeLayout.getRecipeSlots();
		IRecipeSlotsView recipeSlotsView = recipeSlots.getView();

		try {
			return transferHandler.transferRecipe(container, recipeLayout.getRecipe(), recipeSlotsView, player, maxTransfer, doTransfer);
		} catch (RuntimeException e) {
			LOGGER.error(
					"Recipe transfer handler '{}' for container '{}' and recipe type '{}' threw an error: ",
					transferHandler.getClass(), transferHandler.getContainerClass(), recipeCategory.getRecipeType().getUid(), e
			);
			return RecipeTransferErrorInternal.INSTANCE;
		}
	}

	public static boolean allowsTransfer(@Nullable IRecipeTransferError error) {
		return error == null ||
			error.getType() == IRecipeTransferError.Type.COSMETIC;
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

	@Nullable
	public static <T> Map.Entry<T, ItemStack> containsAnyStackIndexed(IStackHelper stackhelper, Map<T, ItemStack> stacks, IRecipeSlotView recipeSlotView) {
		MatchingIndexed<T> matchingStacks = new MatchingIndexed<>(stacks);
		List<ItemStack> ingredients = recipeSlotView.getItemStacks().toList();
		MatchingIterable matchingContains = new MatchingIterable(ingredients);
		return containsStackMatchable(stackhelper, matchingStacks, matchingContains);
	}

	/* Returns an ItemStack from "stacks" if it isEquivalent to an ItemStack from "contains" */
	@Nullable
	public static <R, T> R containsStackMatchable(
		IStackHelper stackhelper,
		Iterable<ItemStackMatchable<R>> stacks,
		Iterable<ItemStackMatchable<T>> contains
	) {
		for (ItemStackMatchable<?> containStack : contains) {
			R matchingStack = containsStack(stackhelper, stacks, containStack);
			if (matchingStack != null) {
				return matchingStack;
			}
		}

		return null;
	}

	/* Returns an ItemStack from "stacks" if it isEquivalent to "contains" */
	@Nullable
	public static <R> R containsStack(IStackHelper stackHelper, Iterable<ItemStackMatchable<R>> stacks, ItemStackMatchable<?> contains) {
		for (ItemStackMatchable<R> stack : stacks) {
			if (stackHelper.isEquivalent(contains.getStack(), stack.getStack(), UidContext.Recipe)) {
				return stack.getResult();
			}
		}
		return null;
	}

	private static class MatchingIndexed<T> implements Iterable<ItemStackMatchable<Map.Entry<T, ItemStack>>> {
		private final Map<T, ItemStack> map;

		public MatchingIndexed(Map<T, ItemStack> map) {
			this.map = map;
		}

		@Override
		public Iterator<ItemStackMatchable<Map.Entry<T, ItemStack>>> iterator() {
			return new MatchingIterable.DelegateIterator<>(map.entrySet().iterator()) {
				@Override
				public ItemStackMatchable<Map.Entry<T, ItemStack>> next() {
					final Map.Entry<T, ItemStack> entry = delegate.next();
					return new ItemStackMatchable<>() {
						@Override
						public ItemStack getStack() {
							return entry.getValue();
						}

						@Override
						public Map.Entry<T, ItemStack> getResult() {
							return entry;
						}
					};
				}
			};
		}
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
