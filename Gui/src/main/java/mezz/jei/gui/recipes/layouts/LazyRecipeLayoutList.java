package mezz.jei.gui.recipes.layouts;

import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.transfer.IRecipeTransferManager;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.common.Internal;
import mezz.jei.common.config.RecipeSorterStage;
import mezz.jei.gui.bookmarks.BookmarkList;
import mezz.jei.gui.bookmarks.RecipeBookmark;
import mezz.jei.gui.recipes.RecipeBookmarkButton;
import mezz.jei.gui.recipes.RecipeLayoutWithButtons;
import mezz.jei.gui.recipes.RecipeSortUtil;
import mezz.jei.gui.recipes.RecipeTransferButton;
import mezz.jei.gui.recipes.RecipesGui;
import mezz.jei.gui.recipes.lookups.IFocusedRecipes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class LazyRecipeLayoutList<T> implements IRecipeLayoutList {
	private final @Nullable AbstractContainerMenu container;
	private final @Nullable Player player;
	private final IRecipeManager recipeManager;
	private final IRecipeCategory<T> recipeCategory;
	private final RecipesGui recipesGui;
	private final IFocusGroup focusGroup;
	private final List<RecipeLayoutWithButtons<?>> results;
	private final List<RecipeLayoutWithButtons<?>> craftMissing;
	private final Iterator<T> unsortedIterator;
	private final int size;

	private final boolean matchingCraftable;
	private final BookmarkList bookmarkList;

	public LazyRecipeLayoutList(
		Set<RecipeSorterStage> recipeSorterStages,
		@Nullable AbstractContainerMenu container,
		@Nullable Player player,
		IFocusedRecipes<T> selectedRecipes,
		BookmarkList bookmarkList,
		IRecipeManager recipeManager,
		RecipesGui recipesGui,
		IFocusGroup focusGroup
	) {
		this.bookmarkList = bookmarkList;
		boolean matchingBookmarks = recipeSorterStages.contains(RecipeSorterStage.BOOKMARKED);
		boolean matchingCraftable = recipeSorterStages.contains(RecipeSorterStage.CRAFTABLE);
		this.container = container;
		this.player = player;
		this.recipeManager = recipeManager;
		this.recipesGui = recipesGui;
		this.focusGroup = focusGroup;
		this.results = new ArrayList<>();
		this.craftMissing = new ArrayList<>();
		this.recipeCategory = selectedRecipes.getRecipeCategory();

		List<T> recipes = selectedRecipes.getRecipes();
		this.size = recipes.size();

		if (matchingCraftable && container != null) {
			IRecipeTransferManager recipeTransferManager = Internal.getJeiRuntime().getRecipeTransferManager();
			this.matchingCraftable = recipeTransferManager.getRecipeTransferHandler(container, recipeCategory).isPresent();
		} else {
			this.matchingCraftable = false;
		}

		if (matchingBookmarks) {
			// if bookmarks go first, start by grabbing all the bookmarked elements, it's relatively cheap
			RecipeType<T> recipeType = recipeCategory.getRecipeType();

			recipes = new ArrayList<>(recipes);
			Iterator<T> iterator = recipes.iterator();
			while (iterator.hasNext()) {
				T recipe = iterator.next();
				RecipeBookmark<T, ?> recipeBookmark = bookmarkList.getMatchingBookmark(recipeType, recipe);
				if (recipeBookmark != null) {
					IRecipeLayoutDrawable<T> recipeLayout = recipeManager.createRecipeLayoutDrawableOrShowError(recipeCategory, recipe, focusGroup);
					RecipeLayoutWithButtons<T> recipeLayoutWithButtons = createRecipeLayoutWithButtons(recipeLayout, recipeBookmark, bookmarkList, recipesGui, container, player);
					results.add(recipeLayoutWithButtons);
					iterator.remove();
				}
			}
		}

		this.unsortedIterator = recipes.iterator();
	}

	private static <T> RecipeLayoutWithButtons<T> createRecipeLayoutWithButtons(
		IRecipeLayoutDrawable<T> recipeLayoutDrawable,
		RecipeBookmark<?, ?> recipeBookmark,
		BookmarkList bookmarks,
		RecipesGui recipesGui,
		@Nullable AbstractContainerMenu container,
		@Nullable Player player
	) {
		RecipeTransferButton transferButton = RecipeTransferButton.create(recipeLayoutDrawable, recipesGui::onClose, container, player);

		RecipeBookmarkButton bookmarkButton = RecipeBookmarkButton.create(
			recipeLayoutDrawable,
			bookmarks,
			recipeBookmark
		);

		return new RecipeLayoutWithButtons<>(recipeLayoutDrawable, transferButton, bookmarkButton);
	}

	private IRecipeLayoutDrawable<T> createRecipeLayout(T recipe) {
		return recipeManager.createRecipeLayoutDrawableOrShowError(recipeCategory, recipe, focusGroup);
	}

	private RecipeLayoutWithButtons<T> createRecipeLayoutWithButtons(IRecipeLayoutDrawable<T> recipeLayoutDrawable, IIngredientManager ingredientManager) {
		RecipeTransferButton transferButton = RecipeTransferButton.create(recipeLayoutDrawable, recipesGui::onClose, container, player);
		RecipeBookmarkButton bookmarkButton = RecipeBookmarkButton.create(recipeLayoutDrawable, ingredientManager, bookmarkList);
		return new RecipeLayoutWithButtons<>(recipeLayoutDrawable, transferButton, bookmarkButton);
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public List<RecipeLayoutWithButtons<?>> subList(int from, int to) {
		ensureResults(to - 1);
		return results.subList(from, to);
	}

	private void ensureResults(int index) {
		while (index >= results.size()) {
			if (!calculateNextResult()) {
				return;
			}
		}
	}

	@Override
	public Optional<RecipeLayoutWithButtons<?>> findFirst() {
		ensureResults(0);
		if (results.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(results.getFirst());
	}

	@Override
	public void tick() {
		calculateNextResult();
	}

	private boolean calculateNextResult() {
		IJeiRuntime jeiRuntime = Internal.getJeiRuntime();
		IIngredientManager ingredientManager = jeiRuntime.getIngredientManager();

		while (unsortedIterator.hasNext()) {
			T recipe = unsortedIterator.next();
			IRecipeLayoutDrawable<T> recipeLayout = createRecipeLayout(recipe);
			RecipeLayoutWithButtons<T> recipeLayoutWithButtons = createRecipeLayoutWithButtons(recipeLayout, ingredientManager);
			RecipeTransferButton transferButton = recipeLayoutWithButtons.transferButton();

			if (matchingCraftable) {
				// if craftables go first, look for a 100% craftable element
				int missingCountHint = transferButton.getMissingCountHint();
				if (missingCountHint == 0) {
					results.add(recipeLayoutWithButtons);
					return true;
				} else {
					craftMissing.add(recipeLayoutWithButtons);
				}
			} else {
				results.add(recipeLayoutWithButtons);
				return true;
			}
		}

		// from here we're finished with calculating all the transfer handlers,
		// just sort and add everything left to the results
		if (!craftMissing.isEmpty()) {
			craftMissing.sort(RecipeSortUtil.getCraftableComparator());
			results.addAll(craftMissing);
			craftMissing.clear();
			return true;
		}

		return false;
	}
}
