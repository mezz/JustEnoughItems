package mezz.jei.transfer;

import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

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

		int recipeSlotNumber = -1;
		SortedSet<Integer> keys = new TreeSet<>(ingredientsMap.keySet());
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

			Integer matching = containsAnyStackIndexed(stackhelper, availableItemStacks, requiredStacks);
			if (matching == null) {
				matchingItemResult.missingItems.add(key);
			} else {
				ItemStack matchingStack = availableItemStacks.get(matching);
				matchingStack.shrink(1);
				if (matchingStack.getCount() == 0) {
					availableItemStacks.remove(matching);
				}
				matchingItemResult.matchingItems.put(recipeSlotNumber, matching);
			}
		}

		return matchingItemResult;
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
			return new MatchingIterable.DelegateIterator<>(map.entrySet().iterator()) {
				@Override
				public ItemStackMatchable<Integer> next() {
					final Map.Entry<Integer, ItemStack> entry = delegate.next();
					return new ItemStackMatchable<>() {
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
