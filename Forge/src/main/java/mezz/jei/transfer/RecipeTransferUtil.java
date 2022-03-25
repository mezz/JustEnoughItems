package mezz.jei.transfer;

import mezz.jei.Internal;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IStackHelper;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.gui.ingredients.RecipeSlots;
import mezz.jei.gui.recipes.RecipeLayout;
import mezz.jei.recipes.RecipeTransferManager;
import mezz.jei.runtime.JeiRuntime;
import mezz.jei.util.ItemStackMatchable;
import mezz.jei.util.MatchingIterable;
import mezz.jei.common.util.StringUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.jetbrains.annotations.Nullable;
import java.util.Collection;
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
	public static IRecipeTransferError getTransferRecipeError(RecipeTransferManager recipeTransferManager, AbstractContainerMenu container, RecipeLayout<?> recipeLayout, Player player) {
		return transferRecipe(recipeTransferManager, container, recipeLayout, player, false, false);
	}

	public static boolean transferRecipe(RecipeTransferManager recipeTransferManager, AbstractContainerMenu container, RecipeLayout<?> recipeLayout, Player player, boolean maxTransfer) {
		IRecipeTransferError error = transferRecipe(recipeTransferManager, container, recipeLayout, player, maxTransfer, true);
		return allowsTransfer(error);
	}

	@SuppressWarnings("removal")
	@Nullable
	private static <C extends AbstractContainerMenu, R> IRecipeTransferError transferRecipe(
		RecipeTransferManager recipeTransferManager,
		C container,
		RecipeLayout<R> recipeLayout,
		Player player,
		boolean maxTransfer,
		boolean doTransfer
	) {
		final JeiRuntime runtime = Internal.getRuntime();
		if (runtime == null) {
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
		} catch (UnsupportedOperationException ignored) {
			// old handlers do not support calling the new transferRecipe method.
			// call the legacy method instead
			return transferHandler.transferRecipe(container, recipeLayout.getRecipe(), recipeLayout.getLegacyAdapter(), player, maxTransfer, doTransfer);
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

		for (int i = 0; i < requiredItemStacks.size(); i++) {
			IRecipeSlotView requiredItemStack = requiredItemStacks.get(i);
			if (requiredItemStack.isEmpty()) {
				continue;
			}
			Slot craftingSlot = craftingSlots.get(i);

			Map.Entry<Slot, ItemStack> matching = containsAnyStackIndexed(stackhelper, availableItemStacks, requiredItemStack);
			if (matching == null) {
				transferOperations.missingItems.add(requiredItemStack);
			} else {
				Slot matchingSlot = matching.getKey();
				ItemStack matchingStack = matching.getValue();
				matchingStack.shrink(1);
				if (matchingStack.isEmpty()) {
					availableItemStacks.remove(matchingSlot);
				}
				transferOperations.results.add(new TransferOperation(matchingSlot, craftingSlot));
			}
		}

		return transferOperations;
	}

	@Nullable
	public static <T> Map.Entry<T, ItemStack> containsAnyStackIndexed(IStackHelper stackhelper, Map<T, ItemStack> stacks, IRecipeSlotView recipeSlotView) {
		MatchingIndexed<T> matchingStacks = new MatchingIndexed<>(stacks);
		MatchingIterable matchingContains = new MatchingIterable(recipeSlotView);
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
}
