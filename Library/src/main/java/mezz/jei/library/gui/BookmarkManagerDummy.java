package mezz.jei.library.gui;

import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IBookmarkManager;

public class BookmarkManagerDummy implements IBookmarkManager {
	public static final IBookmarkManager INSTANCE = new BookmarkManagerDummy();

	private BookmarkManagerDummy() {

	}

	@Override
	public boolean contains(ITypedIngredient<?> ingredient) {
		return false;
	}

	@Override
	public boolean add(ITypedIngredient<?> ingredient) {
		return false;
	}

	@Override
	public boolean remove(ITypedIngredient<?> ingredient) {
		return false;
	}
}
