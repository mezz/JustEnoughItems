package mezz.jei.transfer;

import mezz.jei.Internal;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.helpers.IStackHelper;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.gui.recipes.RecipeLayout;
import mezz.jei.recipes.RecipeTransferManager;
import mezz.jei.runtime.JeiRuntime;
import mezz.jei.util.ItemStackMatchable;
import mezz.jei.util.MatchingIterable;
import mezz.jei.util.StringUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
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
	public static IRecipeTransferError getTransferRecipeError(RecipeTransferManager recipeTransferManager, AbstractContainerMenu container, RecipeLayout<?> recipeLayout, Player player) {
		return transferRecipe(recipeTransferManager, container, recipeLayout, player, false, false);
	}

	public static boolean transferRecipe(RecipeTransferManager recipeTransferManager, AbstractContainerMenu container, RecipeLayout<?> recipeLayout, Player player, boolean maxTransfer) {
		IRecipeTransferError error = transferRecipe(recipeTransferManager, container, recipeLayout, player, maxTransfer, true);
		return allowsTransfer(error);
	}

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

		RecipeSlotsView recipeSlotsView = recipeLayout.createRecipeSlotsView();
		return transferHandler.transferRecipe(container, recipeLayout.getRecipe(), recipeSlotsView, player, maxTransfer, doTransfer);
	}

	public static boolean allowsTransfer(@Nullable IRecipeTransferError error) {
		return error == null ||
			error.getType() == IRecipeTransferError.Type.COSMETIC;
	}

	public static boolean validateSlots(
		Player player,
		Collection<Slot> recipeTargetSlots,
		Collection<Slot> ingredientTargetSlots,
		List<Slot> craftingSlots,
		List<Slot> inventorySlots
	) {
		Set<Integer> inventorySlotIndexes = inventorySlots.stream()
			.map(s -> s.index)
			.collect(Collectors.toSet());
		Set<Integer> craftingSlotIndexes = craftingSlots.stream()
			.map(s -> s.index)
			.collect(Collectors.toSet());

		// check that all recipeSlotToSourceSlots recipe slots are included in craftingSlots
		{
			List<Integer> invalidRecipeIndexes = recipeTargetSlots.stream()
				.map(s -> s.index)
				.filter(s -> !craftingSlotIndexes.contains(s))
				.toList();
			if (!invalidRecipeIndexes.isEmpty()) {
				LOGGER.error(
					"Transfer handler has invalid slots for the origin of the recipe, " +
						"the slots are not included in the list of crafting slots. " +
						StringUtil.intsToString(invalidRecipeIndexes)
				);
				return false;
			}
		}

		// check that all recipeSlotToSourceSlots inventory slots are included in inventorySlots or recipeSlots
		{
			List<Integer> invalidInventorySlotIndexes = ingredientTargetSlots.stream()
				.map(s -> s.index)
				.filter(s -> !inventorySlotIndexes.contains(s) && !craftingSlotIndexes.contains(s))
				.toList();
			if (!invalidInventorySlotIndexes.isEmpty()) {
				LOGGER.error(
					"Transfer handler has invalid slots for the inventory stacks for the recipe, " +
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

	public static class MatchingItemsResult {
		public final Map<IRecipeSlotView, Slot> matchingItems = new IdentityHashMap<>();
		public final List<IRecipeSlotView> missingItems = new ArrayList<>();
	}

	/**
	 * Returns a list of items in slots that complete the recipe defined by requiredStacksList.
	 * Returns a result that contains missingItems if there are not enough items in availableItemStacks.
	 */
	public static MatchingItemsResult getMatchingItems(
		IStackHelper stackhelper,
		Map<Slot, ItemStack> availableItemStacks,
		List<IRecipeSlotView> slotsView
	) {
		MatchingItemsResult matchingItemResult = new MatchingItemsResult();

		slotsView.stream()
			.filter(ingredient -> ingredient.getRole() == RecipeIngredientRole.INPUT)
			.forEach(ingredient -> {
				Map.Entry<Slot, ItemStack> matching = containsAnyStackIndexed(stackhelper, availableItemStacks, ingredient);
				if (matching == null) {
					matchingItemResult.missingItems.add(ingredient);
				} else {
					Slot matchingSlot = matching.getKey();
					ItemStack matchingStack = matching.getValue();
					matchingStack.shrink(1);
					if (matchingStack.isEmpty()) {
						availableItemStacks.remove(matchingSlot);
					}
					matchingItemResult.matchingItems.put(ingredient, matchingSlot);
				}
			});

		return matchingItemResult;
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
