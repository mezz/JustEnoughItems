package mezz.jei.transfer;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;

import mezz.jei.Internal;
import mezz.jei.api.gui.ingredient.IGuiIngredient;
import mezz.jei.api.helpers.IStackHelper;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.gui.recipes.RecipeLayout;
import mezz.jei.recipes.RecipeTransferManager;
import mezz.jei.runtime.JeiRuntime;
import mezz.jei.util.ItemStackMatchable;
import mezz.jei.util.MatchingIterable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class RecipeTransferUtil {
	private static final Logger LOGGER = LogManager.getLogger();

	private RecipeTransferUtil() {
	}

	@Nullable
	public static IRecipeTransferError getTransferRecipeError(RecipeTransferManager recipeTransferManager, Container container, RecipeLayout<?> recipeLayout, PlayerEntity player) {
		return transferRecipe(recipeTransferManager, container, recipeLayout, player, false, false);
	}

	public static boolean transferRecipe(RecipeTransferManager recipeTransferManager, Container container, RecipeLayout<?> recipeLayout, PlayerEntity player, boolean maxTransfer) {
		IRecipeTransferError error = transferRecipe(recipeTransferManager, container, recipeLayout, player, maxTransfer, true);
		return allowsTransfer(error);
	}

	@Nullable
	private static <T extends Container> IRecipeTransferError transferRecipe(RecipeTransferManager recipeTransferManager, T container, RecipeLayout<?> recipeLayout, PlayerEntity player, boolean maxTransfer, boolean doTransfer) {
		final JeiRuntime runtime = Internal.getRuntime();
		if (runtime == null) {
			return RecipeTransferErrorInternal.INSTANCE;
		}

		final IRecipeTransferHandler<? super T> transferHandler = recipeTransferManager.getRecipeTransferHandler(container, recipeLayout.getRecipeCategory());
		if (transferHandler == null) {
			if (doTransfer) {
				LOGGER.error("No Recipe Transfer handler for container {}", container.getClass());
			}
			return RecipeTransferErrorInternal.INSTANCE;
		}

		return transferHandler.transferRecipe(container, recipeLayout.getRecipe(), recipeLayout, player, maxTransfer, doTransfer);
	}

	public static boolean allowsTransfer(@Nullable IRecipeTransferError error) {
		return error == null ||
			error.getType() == IRecipeTransferError.Type.COSMETIC;
	}

	public static class MatchingItemsResult {
		public final Map<Integer, Integer> matchingItems = new HashMap<>();
		public final List<Integer> missingItems = new ArrayList<>();
	}

	/**
	 * Returns a list of items in slots that complete the recipe defined by requiredStacksList.
	 * Returns a result that contains missingItems if there are not enough items in availableItemStacks.
	 */
	public static MatchingItemsResult getMatchingItems(IStackHelper stackhelper, Map<Integer, ItemStack> availableItemStacks, Map<Integer, ? extends IGuiIngredient<ItemStack>> ingredientsMap) {
		MatchingItemsResult matchingItemResult = new MatchingItemsResult();
		List<RequiredIngredient> requiredIngredients = new ArrayList<>();
		int recipeSlotNumber = -1;
		List<Integer> keys = new ArrayList<>(ingredientsMap.keySet());
		Collections.sort(keys);
		for (Integer key : keys) {
			IGuiIngredient<ItemStack> ingredient = ingredientsMap.get(key);
			if (!ingredient.isInput()) {
				continue;
			}
			recipeSlotNumber++;

			List<ItemStack> requiredStacks = ingredient.getAllIngredients();
			if (requiredStacks.isEmpty()) {
				continue;
			}

			List<Integer> candidateSlots = new ArrayList<>();
			for (Map.Entry<Integer, ItemStack> availableEntry : availableItemStacks.entrySet()) {
				for (ItemStack requiredStack : requiredStacks) {
					if (stackhelper.isEquivalent(requiredStack, availableEntry.getValue(), UidContext.Recipe)) {
						candidateSlots.add(availableEntry.getKey());
						break;
					}
				}
			}
			Collections.sort(candidateSlots, (first, second) -> {
				int compare = Integer.compare(availableItemStacks.get(first).getCount(), availableItemStacks.get(second).getCount());
				return compare == 0 ? Integer.compare(first, second) : compare;
			});
			requiredIngredients.add(new RequiredIngredient(recipeSlotNumber, key, candidateSlots));
		}

		Map<Integer, Integer> availableCounts = new HashMap<>();
		for (Map.Entry<Integer, ItemStack> entry : availableItemStacks.entrySet()) {
			availableCounts.put(entry.getKey(), entry.getValue().getCount());
		}

		Map<Integer, Integer> assignments = new HashMap<>();
		Map<Integer, Integer> bestAssignments = new HashMap<>();
		assignRequiredIngredients(
			requiredIngredients,
			availableCounts,
			new HashSet<Integer>(),
			assignments,
			bestAssignments
		);

		for (int i = 0; i < requiredIngredients.size(); i++) {
			RequiredIngredient requiredIngredient = requiredIngredients.get(i);
			Integer matchingSlot = bestAssignments.get(i);
			if (matchingSlot == null) {
				matchingItemResult.missingItems.add(requiredIngredient.guiSlotNumber);
			} else {
				matchingItemResult.matchingItems.put(requiredIngredient.recipeSlotNumber, matchingSlot);
			}
		}

		return matchingItemResult;
	}

	private static boolean assignRequiredIngredients(
		List<RequiredIngredient> requiredIngredients,
		Map<Integer, Integer> availableCounts,
		Set<Integer> processedIndexes,
		Map<Integer, Integer> assignments,
		Map<Integer, Integer> bestAssignments
	) {
		if (assignments.size() > bestAssignments.size()) {
			bestAssignments.clear();
			bestAssignments.putAll(assignments);
		}

		if (processedIndexes.size() == requiredIngredients.size()) {
			return assignments.size() == requiredIngredients.size();
		}

		int requiredIndex = getMostConstrainedIngredient(requiredIngredients, availableCounts, processedIndexes);
		processedIndexes.add(requiredIndex);
		RequiredIngredient requiredIngredient = requiredIngredients.get(requiredIndex);
		for (Integer candidateSlot : requiredIngredient.candidateSlots) {
			int availableCount = availableCounts.getOrDefault(candidateSlot, 0);
			if (availableCount <= 0) {
				continue;
			}

			availableCounts.put(candidateSlot, availableCount - 1);
			assignments.put(requiredIndex, candidateSlot);
			if (assignRequiredIngredients(requiredIngredients, availableCounts, processedIndexes, assignments, bestAssignments)) {
				return true;
			}
			assignments.remove(requiredIndex);
			availableCounts.put(candidateSlot, availableCount);
		}

		assignRequiredIngredients(requiredIngredients, availableCounts, processedIndexes, assignments, bestAssignments);
		processedIndexes.remove(requiredIndex);
		return false;
	}

	private static int getMostConstrainedIngredient(
		List<RequiredIngredient> requiredIngredients,
		Map<Integer, Integer> availableCounts,
		Set<Integer> processedIndexes
	) {
		int bestIndex = -1;
		int bestCandidateCount = Integer.MAX_VALUE;
		for (int i = 0; i < requiredIngredients.size(); i++) {
			if (processedIndexes.contains(i)) {
				continue;
			}

			int candidateCount = 0;
			for (Integer candidateSlot : requiredIngredients.get(i).candidateSlots) {
				if (availableCounts.getOrDefault(candidateSlot, 0) > 0) {
					candidateCount++;
				}
			}
			if (candidateCount < bestCandidateCount) {
				bestIndex = i;
				bestCandidateCount = candidateCount;
			}
		}
		return bestIndex;
	}

	private static final class RequiredIngredient {
		private final int recipeSlotNumber;
		private final int guiSlotNumber;
		private final List<Integer> candidateSlots;

		private RequiredIngredient(int recipeSlotNumber, int guiSlotNumber, List<Integer> candidateSlots) {
			this.recipeSlotNumber = recipeSlotNumber;
			this.guiSlotNumber = guiSlotNumber;
			this.candidateSlots = candidateSlots;
		}
	}

	@Nullable
	public static Integer containsAnyStackIndexed(IStackHelper stackhelper, Map<Integer, ItemStack> stacks, Iterable<ItemStack> contains) {
		MatchingIndexed matchingStacks = new MatchingIndexed(stacks);
		MatchingIterable matchingContains = new MatchingIterable(contains);
		return containsStackMatchable(stackhelper, matchingStacks, matchingContains);
	}

	/* Returns an ItemStack from "stacks" if it isEquivalent to an ItemStack from "contains" */
	@Nullable
	public static <R, T> R containsStackMatchable(IStackHelper stackhelper, Iterable<ItemStackMatchable<R>> stacks, Iterable<ItemStackMatchable<T>> contains) {
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

	private static class MatchingIndexed implements Iterable<ItemStackMatchable<Integer>> {
		private final Map<Integer, ItemStack> map;

		public MatchingIndexed(Map<Integer, ItemStack> map) {
			this.map = map;
		}

		@Override
		public Iterator<ItemStackMatchable<Integer>> iterator() {
			return new MatchingIterable.DelegateIterator<Map.Entry<Integer, ItemStack>, ItemStackMatchable<Integer>>(map.entrySet().iterator()) {
				@Override
				public ItemStackMatchable<Integer> next() {
					final Map.Entry<Integer, ItemStack> entry = delegate.next();
					return new ItemStackMatchable<Integer>() {
						@Override
						public ItemStack getStack() {
							return entry.getValue();
						}

						@Override
						public Integer getResult() {
							return entry.getKey();
						}
					};
				}
			};
		}
	}
}
