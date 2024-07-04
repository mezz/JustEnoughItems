package mezz.jei.gui.bookmarks;

import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.gui.config.IBookmarkConfig;
import mezz.jei.gui.overlay.elements.IElement;
import mezz.jei.gui.overlay.IIngredientGridSource;
import net.minecraft.core.RegistryAccess;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class BookmarkList implements IIngredientGridSource {
	private final List<IBookmark> bookmarks = new LinkedList<>();
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
		if (contains(value)) {
			return false;
		}
		addToList(value, clientConfig.isAddingBookmarksToFrontEnabled());
		notifyListenersOfChange();
		bookmarkConfig.saveBookmarks(recipeManager, focusFactory, guiHelper, ingredientManager, registryAccess, bookmarks);
		return true;
	}

	public boolean contains(IBookmark value) {
		return this.bookmarks.contains(value);
	}

	public <T> boolean toggleBookmark(IElement<T> element) {
		IBookmark bookmark = element.getBookmark()
			.orElseGet(() -> {
				ITypedIngredient<T> ingredient = element.getTypedIngredient();
				return IngredientBookmark.create(ingredient, ingredientManager);
			});
		return toggleBookmark(bookmark);
	}

	public boolean toggleBookmark(IBookmark bookmark) {
		return remove(bookmark) ||
			add(bookmark);
	}

	public boolean remove(IBookmark ingredient) {
		if (!bookmarks.remove(ingredient)) {
			return false;
		}

		notifyListenersOfChange();
		bookmarkConfig.saveBookmarks(recipeManager, focusFactory, guiHelper, ingredientManager, registryAccess, bookmarks);
		return true;
	}

	public void addToList(IBookmark value, boolean addToFront) {
		if (addToFront) {
			bookmarks.addFirst(value);
		} else {
			bookmarks.add(value);
		}
	}

	@Override
	public List<IElement<?>> getElements() {
		return bookmarks.stream()
			.<IElement<?>>map(IBookmark::getElement)
			.toList();
	}

	public boolean isEmpty() {
		return bookmarks.isEmpty();
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
