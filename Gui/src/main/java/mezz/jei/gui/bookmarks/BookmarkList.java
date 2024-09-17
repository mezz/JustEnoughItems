package mezz.jei.gui.bookmarks;

import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.gui.config.IBookmarkConfig;
import mezz.jei.gui.overlay.IIngredientGridSource;
import mezz.jei.gui.overlay.elements.IElement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class BookmarkList implements IIngredientGridSource {
	private final List<IBookmark> bookmarksList = new LinkedList<>();
	private final Set<IBookmark> bookmarksSet = new HashSet<>();

	private final IIngredientManager ingredientManager;
	private final IBookmarkConfig bookmarkConfig;
	private final IClientConfig clientConfig;
	private final List<SourceListChangedListener> listeners = new ArrayList<>();

	public BookmarkList(
		IIngredientManager ingredientManager,
		IBookmarkConfig bookmarkConfig,
		IClientConfig clientConfig
	) {
		this.ingredientManager = ingredientManager;
		this.bookmarkConfig = bookmarkConfig;
		this.clientConfig = clientConfig;
	}

	public boolean add(IBookmark value) {
		if (!addToListWithoutNotifying(value, clientConfig.isAddingBookmarksToFrontEnabled())) {
			return false;
		}
		notifyListenersOfChange();
		bookmarkConfig.saveBookmarks(ingredientManager, bookmarksList);
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
		bookmarkConfig.saveBookmarks(ingredientManager, bookmarksList);
	}

	public boolean contains(IBookmark value) {
		return this.bookmarksSet.contains(value);
	}

	public <T> boolean onElementBookmarked(IElement<T> element) {
		return element.getBookmark()
			.map(this::remove)
			.orElseGet(() -> {
				ITypedIngredient<T> ingredient = element.getTypedIngredient();
				IBookmark bookmark = IngredientBookmark.create(ingredient, ingredientManager);
				return add(bookmark);
			});
	}

	public void toggleBookmark(IBookmark bookmark) {
		if (remove(bookmark)) {
			return;
		}
		add(bookmark);
	}

	public boolean remove(IBookmark ingredient) {
		if (!bookmarksSet.remove(ingredient)) {
			return false;
		}
		bookmarksList.remove(ingredient);

		notifyListenersOfChange();
		bookmarkConfig.saveBookmarks(ingredientManager, bookmarksList);
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

	@Override
	public List<IElement<?>> getElements() {
		return bookmarksList.stream()
			.<IElement<?>>map(IBookmark::getElement)
			.toList();
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
}
