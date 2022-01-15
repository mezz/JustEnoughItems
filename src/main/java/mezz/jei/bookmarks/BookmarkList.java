package mezz.jei.bookmarks;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.config.BookmarkConfig;
import mezz.jei.gui.overlay.IIngredientGridSource;
import mezz.jei.ingredients.IngredientManager;
import net.minecraft.world.item.ItemStack;

public class BookmarkList implements IIngredientGridSource {
	private final List<Object> list = new LinkedList<>();
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
		addToList(ingredient, true);
		notifyListenersOfChange();
		bookmarkConfig.saveBookmarks(ingredientManager, list);
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
		notifyListenersOfChange();
		bookmarkConfig.saveBookmarks(ingredientManager, list);
		return true;
	}

	public <T> void addToList(T ingredient, boolean addToFront) {
		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(ingredient);
		ingredient = ingredientHelper.normalizeIngredient(ingredient);

		if (addToFront) {
			list.add(0, ingredient);
		} else {
			list.add(ingredient);
		}
	}

	@Override
	public List<?> getIngredientList(String filterText) {
		return list;
	}

	public boolean isEmpty() {
		return list.isEmpty();
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
