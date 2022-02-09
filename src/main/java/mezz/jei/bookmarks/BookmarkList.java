package mezz.jei.bookmarks;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.config.BookmarkConfig;
import mezz.jei.gui.overlay.IIngredientGridSource;
import mezz.jei.ingredients.IngredientManager;
import mezz.jei.ingredients.TypedIngredient;
import net.minecraft.world.item.ItemStack;

public class BookmarkList implements IIngredientGridSource {
	private final List<ITypedIngredient<?>> list = new LinkedList<>();
	private final IngredientManager ingredientManager;
	private final BookmarkConfig bookmarkConfig;
	private final List<IIngredientGridSource.Listener> listeners = new ArrayList<>();

	public BookmarkList(IngredientManager ingredientManager, BookmarkConfig bookmarkConfig) {
		this.ingredientManager = ingredientManager;
		this.bookmarkConfig = bookmarkConfig;
	}

	public <T> boolean add(ITypedIngredient<T> value) {
		if (contains(value)) {
			return false;
		}
		addToList(value, true);
		notifyListenersOfChange();
		bookmarkConfig.saveBookmarks(ingredientManager, list);
		return true;
	}

	private <T> boolean contains(ITypedIngredient<T> value) {
		return indexOf(value) >= 0;
	}

	private <T> int indexOf(ITypedIngredient<T> value) {
		// We cannot assume that ingredients have a working equals() implementation. Even ItemStack doesn't have one...
		Optional<ITypedIngredient<T>> normalized = TypedIngredient.normalize(ingredientManager, value);
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

	private static <T> boolean equal(IIngredientHelper<T> ingredientHelper, ITypedIngredient<T> a, String uidA, ITypedIngredient<?> b) {
		if (a.getIngredient() == b.getIngredient()) {
			return true;
		}

		if (a.getIngredient() instanceof ItemStack itemStackA && b.getIngredient() instanceof ItemStack itemStackB) {
			return ItemStack.matches(itemStackA, itemStackB);
		}

		Optional<ITypedIngredient<T>> castB = TypedIngredient.optionalCast(b, a.getType());
		if (castB.isPresent()) {
			T ingredientB = castB.get().getIngredient();
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
		Optional<ITypedIngredient<T>> result = TypedIngredient.normalize(ingredientManager, value);
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
	public List<ITypedIngredient<?>> getIngredientList(String filterText) {
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
