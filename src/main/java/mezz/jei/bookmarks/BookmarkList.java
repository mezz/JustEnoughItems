package mezz.jei.bookmarks;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.config.BookmarkConfig;
import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.gui.overlay.IIngredientGridSource;
import mezz.jei.ingredients.IngredientListElementFactory;
import mezz.jei.ingredients.IngredientManager;
import net.minecraft.item.ItemStack;

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
		if (contains(ingredient)) {
			return false;
		}
		addToLists(ingredient, true);
		notifyListenersOfChange();
		bookmarkConfig.saveBookmarks(ingredientManager, ingredientListElements);
		return true;
	}

	private <T> boolean contains(T ingredient) {
		return indexOf(ingredient) >= 0;
	}

	private <T> int indexOf(T ingredient) {
		// We cannot assume that ingredients have a working equals() implementation. Even ItemStack doesn't have one...
		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(ingredient);
		ingredient = ingredientHelper.normalizeIngredient(ingredient);
		String uniqueId = ingredientHelper.getUniqueId(ingredient, UidContext.Ingredient);

		for (int i = 0; i < list.size(); i++) {
			Object existing = list.get(i);
			if (equal(ingredientHelper, ingredient, uniqueId, existing)) {
				return i;
			}
		}
		return -1;
	}

	private static <T> boolean equal(IIngredientHelper<T> ingredientHelper, T a, String uidA, Object b) {
		if (a == b) {
			return true;
		}
		if (!a.getClass().isInstance(b)) {
			return false;
		}
		if (a instanceof ItemStack) {
			return ItemStack.matches((ItemStack) a, (ItemStack) b);
		}

		@SuppressWarnings("unchecked")
		T castB = (T) b;
		String uidB = ingredientHelper.getUniqueId(castB, UidContext.Ingredient);
		return uidA.equals(uidB);
	}

	public <T> boolean remove(T ingredient) {
		int index = indexOf(ingredient);
		if (index < 0) {
			return false;
		}

		list.remove(index);
		ingredientListElements.remove(index);
		notifyListenersOfChange();
		bookmarkConfig.saveBookmarks(ingredientManager, ingredientListElements);
		return true;
	}

	public <T> void addToLists(T ingredient, boolean addToFront) {
		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(ingredient);
		ingredient = ingredientHelper.normalizeIngredient(ingredient);

		IIngredientListElement<T> element = IngredientListElementFactory.createUnorderedElement(ingredient);
		if (addToFront) {
			list.add(0, ingredient);
			ingredientListElements.add(0, element);
		} else {
			list.add(ingredient);
			ingredientListElements.add(element);
		}
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
