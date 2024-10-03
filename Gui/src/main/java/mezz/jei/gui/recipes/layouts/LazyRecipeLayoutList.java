package mezz.jei.gui.recipes.layouts;

import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.transfer.IRecipeTransferManager;
import mezz.jei.common.Internal;
import mezz.jei.common.config.RecipeSorterStage;
import mezz.jei.gui.bookmarks.BookmarkList;
import mezz.jei.gui.bookmarks.RecipeBookmark;
import mezz.jei.gui.recipes.IRecipeLayoutWithButtons;
import mezz.jei.gui.recipes.IRecipeLayoutWithButtonsFactory;
import mezz.jei.gui.recipes.RecipeSortUtil;
import mezz.jei.gui.recipes.lookups.IFocusedRecipes;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class LazyRecipeLayoutList<T> implements IRecipeLayoutList {
	private final IRecipeManager recipeManager;
	private final IRecipeCategory<T> recipeCategory;
	private final IFocusGroup focusGroup;
	private final IRecipeLayoutWithButtonsFactory recipeLayoutFactory;
	private final List<IRecipeLayoutWithButtons<?>> results;
	private final List<IRecipeLayoutWithButtons<?>> craftMissing;
	private final Iterator<T> unsortedIterator;
	private final int size;
	private final boolean sortCraftable;

	LazyRecipeLayoutList(
		Set<RecipeSorterStage> recipeSorterStages,
		IFocusedRecipes<T> selectedRecipes,
		IFocusGroup focusGroup,
		BookmarkList bookmarkList,
		IRecipeManager recipeManager,
		IRecipeLayoutWithButtonsFactory recipeLayoutFactory
	) {
		boolean matchingBookmarks = recipeSorterStages.contains(RecipeSorterStage.BOOKMARKED);
		this.sortCraftable = recipeSorterStages.contains(RecipeSorterStage.CRAFTABLE);
		this.recipeManager = recipeManager;
		this.recipeCategory = selectedRecipes.getRecipeCategory();
		this.focusGroup = focusGroup;
		this.recipeLayoutFactory = recipeLayoutFactory;
		this.results = new ArrayList<>();
		this.craftMissing = new ArrayList<>();

		List<T> recipes = selectedRecipes.getRecipes();
		this.size = recipes.size();

		if (matchingBookmarks) {
			RecipeType<T> recipeType = recipeCategory.getRecipeType();
			recipes = new ArrayList<>(recipes);
			Iterator<T> iterator = recipes.iterator();
			while (iterator.hasNext()) {
				T recipe = iterator.next();
				RecipeBookmark<T, ?> recipeBookmark = bookmarkList.getMatchingBookmark(recipeType, recipe);
				if (recipeBookmark != null) {
					IRecipeLayoutWithButtons<T> recipeLayoutWithButtons = createRecipeLayoutWithButtons(recipe, recipeBookmark);
					this.results.add(recipeLayoutWithButtons);
					iterator.remove();
				}
			}
		}

		this.unsortedIterator = recipes.iterator();
	}

	private IRecipeLayoutWithButtons<T> createRecipeLayoutWithButtons(T recipe, @Nullable RecipeBookmark<?, ?> recipeBookmark) {
		IRecipeLayoutDrawable<T> recipeLayout = recipeManager.createRecipeLayoutDrawableOrShowError(recipeCategory, recipe, focusGroup);
		return recipeLayoutFactory.create(recipeLayout, recipeBookmark);
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public List<IRecipeLayoutWithButtons<?>> subList(int from, int to, @Nullable AbstractContainerMenu container) {
		ensureResults(to - 1, container);
		return results.subList(from, to);
	}

	private void ensureResults(int index, @Nullable AbstractContainerMenu container) {
		while (index >= results.size()) {
			if (!calculateNextResult(container)) {
				return;
			}
		}
	}

	@Override
	public Optional<IRecipeLayoutWithButtons<?>> findFirst(@Nullable AbstractContainerMenu container) {
		ensureResults(0, container);
		if (results.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(results.get(0));
	}

	@Override
	public void tick(@Nullable AbstractContainerMenu container) {
		calculateNextResult(container);
	}

	private boolean calculateNextResult(@Nullable AbstractContainerMenu container) {
		boolean matchingCraftable = isMatchingCraftable(container);
		Player player = Minecraft.getInstance().player;

		while (unsortedIterator.hasNext()) {
			T recipe = unsortedIterator.next();
			IRecipeLayoutWithButtons<T> next = createRecipeLayoutWithButtons(recipe, null);

			if (matchingCraftable) {
				next.updateTransferButton(container, player);
				int missingCountHint = next.getMissingCountHint();
				if (missingCountHint == 0) {
					results.add(next);
					return true;
				} else {
					craftMissing.add(next);
				}
			} else {
				results.add(next);
				return true;
			}
		}

		if (!craftMissing.isEmpty()) {
			craftMissing.sort(RecipeSortUtil.getCraftableComparator());
			results.addAll(craftMissing);
			craftMissing.clear();
			return true;
		}

		return false;
	}

	private boolean isMatchingCraftable(@Nullable AbstractContainerMenu container) {
		if (!sortCraftable || container == null) {
			return false;
		}

		IRecipeTransferManager recipeTransferManager = Internal.getJeiRuntime().getRecipeTransferManager();
		return recipeTransferManager.getRecipeTransferHandler(container, recipeCategory)
			.isPresent();
	}
}
