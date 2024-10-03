package mezz.jei.gui.bookmarks;

import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.gui.config.IBookmarkConfig;
import mezz.jei.gui.input.UserInput;
import mezz.jei.gui.overlay.bookmarks.BookmarkOverlay;
import mezz.jei.gui.overlay.elements.IElement;
import mezz.jei.gui.overlay.ingredients.IIngredientGridSource;
import net.minecraft.core.RegistryAccess;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class BookmarkList implements IIngredientGridSource {
	private final List<IBookmark> bookmarksList = new LinkedList<>();
	private final Set<IBookmark> bookmarksSet = new HashSet<>();

	private final IRecipeManager recipeManager;
	private final IFocusFactory focusFactory;
	private final IIngredientManager ingredientManager;
	private final RegistryAccess registryAccess;
	private final IBookmarkConfig bookmarkConfig;
	private final IClientConfig clientConfig;
	private final IGuiHelper guiHelper;
	private final List<SourceListChangedListener> listeners = new ArrayList<>();

	public BookmarkList(
		IRecipeManager recipeManager,
		IFocusFactory focusFactory,
		IIngredientManager ingredientManager,
		RegistryAccess registryAccess,
		IBookmarkConfig bookmarkConfig,
		IClientConfig clientConfig,
		IGuiHelper guiHelper
	) {
		this.recipeManager = recipeManager;
		this.focusFactory = focusFactory;
		this.ingredientManager = ingredientManager;
		this.registryAccess = registryAccess;
		this.bookmarkConfig = bookmarkConfig;
		this.clientConfig = clientConfig;
		this.guiHelper = guiHelper;
	}

	public boolean add(IBookmark value) {
		if (!addToListWithoutNotifying(value, clientConfig.isAddingBookmarksToFrontEnabled())) {
			return false;
		}
		notifyListenersOfChange();
		saveBookmarks();
		return true;
	}

	public void moveBookmark(IBookmark previousBookmark, IBookmark newBookmark, int offset) {
		if (!bookmarksSet.contains(newBookmark) || !bookmarksSet.contains(previousBookmark)) {
			return;
		}
		int i = bookmarksList.indexOf(previousBookmark);
		int j = bookmarksList.indexOf(newBookmark);
		int newIndex = i + offset;
		if (newIndex == j) {
			return;
		}

		if (newIndex < 0) {
			newIndex += bookmarksList.size();
		}
		newIndex %= bookmarksList.size();

		bookmarksList.remove(newBookmark);
		bookmarksList.add(newIndex, newBookmark);

		notifyListenersOfChange();
		saveBookmarks();
	}

	public boolean contains(IBookmark value) {
		return this.bookmarksSet.contains(value);
	}

	public <T> boolean onElementBookmarked(IElement<T> element) {
		return toggleBookmark(element);
	}

	public <T> boolean onElementBookmarked(IElement<T> element, UserInput input, BookmarkOverlay bookmarkOverlay) {
		if (bookmarkOverlay.isMouseOver(input.getMouseX(), input.getMouseY())) {
			return element.getBookmark()
				.map(this::remove)
				.orElse(false);
		}

		ITypedIngredient<T> ingredient = element.getTypedIngredient();
		IBookmark bookmark = IngredientBookmark.create(ingredient, ingredientManager);
		return add(bookmark);
	}

	public <T> boolean toggleBookmark(IElement<T> element) {
		return element.getBookmark()
			.map(this::remove)
			.orElseGet(() -> {
				ITypedIngredient<T> ingredient = element.getTypedIngredient();
				IBookmark bookmark = IngredientBookmark.create(ingredient, ingredientManager);
				return add(bookmark);
			});
	}

	public <T> boolean addIngredientBookmark(ITypedIngredient<T> ingredient) {
		IBookmark bookmark = IngredientBookmark.create(ingredient, ingredientManager);
		return add(bookmark);
	}

	public boolean toggleBookmark(IBookmark bookmark) {
		return remove(bookmark) ||
			add(bookmark);
	}

	public boolean remove(IBookmark ingredient) {
		if (!bookmarksSet.remove(ingredient)) {
			return false;
		}
		bookmarksList.remove(ingredient);

		notifyListenersOfChange();
		saveBookmarks();
		return true;
	}

	public boolean addToListWithoutNotifying(IBookmark value, boolean addToFront) {
		if (contains(value)) {
			return false;
		}
		if (addToFront) {
			bookmarksList.add(0, value);
			bookmarksSet.add(value);
		} else {
			bookmarksList.add(value);
			bookmarksSet.add(value);
		}
		return true;
	}

	public void addToList(IBookmark value, boolean addToFront) {
		addToListWithoutNotifying(value, addToFront);
	}

	@Override
	public List<IElement<?>> getElements() {
		return bookmarksList.stream()
			.<IElement<?>>map(IBookmark::getElement)
			.toList();
	}

	@Nullable
	public <R> RecipeBookmark<R, ?> getMatchingBookmark(RecipeType<R> recipeType, R recipe) {
		for (IBookmark bookmark : bookmarksList) {
			if (bookmark instanceof RecipeBookmark<?, ?> recipeBookmark && recipeBookmark.isRecipe(recipeType, recipe)) {
				@SuppressWarnings("unchecked")
				RecipeBookmark<R, ?> castBookmark = (RecipeBookmark<R, ?>) recipeBookmark;
				return castBookmark;
			}
		}
		return null;
	}

	public boolean isEmpty() {
		return bookmarksSet.isEmpty();
	}

	@Override
	public void addSourceListChangedListener(SourceListChangedListener listener) {
		listeners.add(listener);
	}

	public void notifyListenersOfChange() {
		for (SourceListChangedListener listener : listeners) {
			listener.onSourceListChanged();
		}
	}

	private void saveBookmarks() {
		bookmarkConfig.saveBookmarks(
			recipeManager,
			focusFactory,
			guiHelper,
			ingredientManager,
			registryAccess,
			bookmarksList
		);
	}
}
