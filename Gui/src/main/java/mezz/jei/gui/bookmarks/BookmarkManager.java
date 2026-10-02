package mezz.jei.gui.bookmarks;

import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.runtime.IBookmarkManager;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.transfer.RecipeTransferService;

public class BookmarkManager implements IBookmarkManager {
	private final BookmarkList bookmarkList;
	private final BookmarkFactory bookmarkFactory;
	private final IRecipeManager recipeManager;
	private final IFocusFactory focusFactory;
	private final IIngredientManager ingredientManager;
	private final RecipeTransferService recipeTransferService;

	public BookmarkManager(
		BookmarkList bookmarkList,
		BookmarkFactory bookmarkFactory,
		IRecipeManager recipeManager,
		IFocusFactory focusFactory,
		IIngredientManager ingredientManager,
		RecipeTransferService recipeTransferService
	) {
		this.bookmarkList = bookmarkList;
		this.bookmarkFactory = bookmarkFactory;
		this.recipeManager = recipeManager;
		this.focusFactory = focusFactory;
		this.ingredientManager = ingredientManager;
		this.recipeTransferService = recipeTransferService;
	}

	@Override
	public boolean contains(ITypedIngredient<?> ingredient) {
		return bookmarkList.contains(bookmarkFactory.create(ingredient));
	}

	@Override
	public boolean add(ITypedIngredient<?> ingredient) {
		return bookmarkList.add(bookmarkFactory.create(ingredient));
	}

	@Override
	public boolean remove(ITypedIngredient<?> ingredient) {
		return bookmarkList.remove(bookmarkFactory.create(ingredient));
	}

	@Override
	public <R> boolean contains(IRecipeType<R> recipeType, R recipe) {
		return bookmarkList.getMatchingBookmark(recipeType, recipe) != null;
	}

	@Override
	public <R> boolean add(IRecipeType<R> recipeType, R recipe) {
		if (contains(recipeType, recipe)) {
			return false;
		}
		IRecipeCategory<R> recipeCategory = recipeManager.getRecipeCategory(recipeType);
		return recipeManager.createRecipeLayoutDrawable(recipeCategory, recipe, focusFactory.getEmptyFocusGroup())
			.map(layout -> RecipeBookmark.create(layout, ingredientManager, recipeTransferService))
			.map(bookmarkList::add)
			.orElse(false);
	}

	@Override
	public <R> boolean remove(IRecipeType<R> recipeType, R recipe) {
		RecipeBookmark<R, ?> bookmark = bookmarkList.getMatchingBookmark(recipeType, recipe);
		return bookmark != null && bookmarkList.remove(bookmark);
	}
}
