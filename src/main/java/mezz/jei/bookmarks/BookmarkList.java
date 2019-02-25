package mezz.jei.bookmarks;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.config.BookmarkConfig;
import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.gui.overlay.IIngredientGridSource;
import mezz.jei.ingredients.IngredientListElementFactory;
import mezz.jei.ingredients.IngredientManager;

public class BookmarkList implements IIngredientGridSource {
	private final List<Object> list = new LinkedList<>();
	private final List<IIngredientListElement<?>> ingredientListElements = new LinkedList<>();
	private final IngredientManager ingredientManager;
	private final BookmarkConfig bookmarkConfig;
	private final List<IIngredientGridSource.Listener> listeners = new ArrayList<>();

	public BookmarkList(IngredientManager ingredientManager, BookmarkConfig bookmarkConfig) {
		this.ingredientManager = ingredientManager;
		this.bookmarkConfig = bookmarkConfig;
	}

	public <T> boolean add(T ingredient) {
		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(ingredient);
		Object normalized = ingredientHelper.normalizeIngredient(ingredient);
		if (!contains(normalized)) {
			if (addToLists(normalized, true)) {
				notifyListenersOfChange();
				bookmarkConfig.saveBookmarks(ingredientManager, ingredientListElements);
				return true;
			}
		}
		return false;
	}

	private boolean contains(Object ingredient) {
		// We cannot assume that ingredients have a working equals() implementation. Even ItemStack doesn't have one...
		IIngredientHelper<Object> ingredientHelper = ingredientManager.getIngredientHelper(ingredient);
		for (Object existing : list) {
			if (ingredient == existing) {
				return true;
			}
			if (existing != null && existing.getClass() == ingredient.getClass()) {
				if (ingredientHelper.getUniqueId(existing).equals(ingredientHelper.getUniqueId(ingredient))) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean remove(Object ingredient) {
		int index = 0;
		for (Object existing : list) {
			if (ingredient == existing) {
				list.remove(index);
				ingredientListElements.remove(index);
				notifyListenersOfChange();
				bookmarkConfig.saveBookmarks(ingredientManager, ingredientListElements);
				return true;
			}
			index++;
		}
		return false;
	}

	public <T> boolean addToLists(T ingredient, boolean addToFront) {
		IIngredientListElement<T> element = IngredientListElementFactory.createUnorderedElement(ingredient);
		if (addToFront) {
			list.add(0, ingredient);
			ingredientListElements.add(0, element);
		} else {
			list.add(ingredient);
			ingredientListElements.add(element);
		}
		return true;
	}

	@Override
	public List<IIngredientListElement<?>> getIngredientList(String filterText) {
		return ingredientListElements;
	}

	public boolean isEmpty() {
		return ingredientListElements.isEmpty();
	}

	@Override
	public void addListener(IIngredientGridSource.Listener listener) {
		listeners.add(listener);
	}

	public void notifyListenersOfChange() {
		for (IIngredientGridSource.Listener listener : listeners) {
			listener.onChange();
		}
	}
}
