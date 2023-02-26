package mezz.jei.gui.bookmarks;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.gui.config.IBookmarkConfig;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.gui.overlay.IIngredientGridSource;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class BookmarkList implements IIngredientGridSource {
	private final List<ITypedIngredient<?>> list = new LinkedList<>();
	private final IIngredientManager ingredientManager;
	private final IBookmarkConfig bookmarkConfig;
	private final IClientConfig clientConfig;
	private final List<SourceListChangedListener> listeners = new ArrayList<>();

	public BookmarkList(IIngredientManager ingredientManager, IBookmarkConfig bookmarkConfig, IClientConfig clientConfig) {
		this.ingredientManager = ingredientManager;
		this.bookmarkConfig = bookmarkConfig;
		this.clientConfig = clientConfig;
	}

	public <T> boolean add(ITypedIngredient<T> value) {
		if (contains(value)) {
			return false;
		}
		addToList(value, clientConfig.isAddingBookmarksToFront());
		notifyListenersOfChange();
		bookmarkConfig.saveBookmarks(ingredientManager, list);
		return true;
	}

	private <T> boolean contains(ITypedIngredient<T> value) {
		return indexOf(value) >= 0;
	}

	private <T> int indexOf(ITypedIngredient<T> value) {
		// We cannot assume that ingredients have a working equals() implementation. Even ItemStack doesn't have one...
		Optional<ITypedIngredient<T>> normalized = normalize(ingredientManager, value);
		if (normalized.isEmpty()) {
			return -1;
		}
		value = normalized.get();

		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(value.getType());
		String uniqueId = ingredientHelper.getUniqueId(value.getIngredient(), UidContext.Ingredient);

		for (int i = 0; i < list.size(); i++) {
			ITypedIngredient<?> existing = list.get(i);
			if (equal(ingredientHelper, value, uniqueId, existing)) {
				return i;
			}
		}
		return -1;
	}

	public static <T> Optional<ITypedIngredient<T>> normalize(IIngredientManager ingredientManager, ITypedIngredient<T> value) {
		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(value.getType());
		T ingredient = ingredientHelper.normalizeIngredient(value.getIngredient());
		return ingredientManager.createTypedIngredient(value.getType(), ingredient);
	}

	private static <T> boolean equal(IIngredientHelper<T> ingredientHelper, ITypedIngredient<T> a, String uidA, ITypedIngredient<?> b) {
		if (a.getIngredient() == b.getIngredient()) {
			return true;
		}

		if (a.getIngredient() instanceof ItemStack itemStackA && b.getIngredient() instanceof ItemStack itemStackB) {
			return ItemStack.matches(itemStackA, itemStackB);
		}

		Optional<T> filteredB = b.getIngredient(a.getType());
		if (filteredB.isPresent()) {
			T ingredientB = filteredB.get();
			String uidB = ingredientHelper.getUniqueId(ingredientB, UidContext.Ingredient);
			return uidA.equals(uidB);
		}

		return false;
	}

	public <T> boolean remove(ITypedIngredient<T> ingredient) {
		int index = indexOf(ingredient);
		if (index < 0) {
			return false;
		}

		list.remove(index);
		notifyListenersOfChange();
		bookmarkConfig.saveBookmarks(ingredientManager, list);
		return true;
	}

	public <T> void addToList(ITypedIngredient<T> value, boolean addToFront) {
		Optional<ITypedIngredient<T>> result = normalize(ingredientManager, value);
		if (result.isEmpty()) {
			return;
		}
		value = result.get();

		if (addToFront) {
			list.add(0, value);
		} else {
			list.add(value);
		}
	}

	@Override
	public List<ITypedIngredient<?>> getIngredientList() {
		return list;
	}

	public boolean isEmpty() {
		return list.isEmpty();
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
