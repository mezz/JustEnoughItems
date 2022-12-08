package mezz.jei.library.gui;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IBookmarkOverlay;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class BookmarkOverlayDummy implements IBookmarkOverlay {
	public static IBookmarkOverlay INSTANCE = new BookmarkOverlayDummy();

	private BookmarkOverlayDummy() {

	}

	@Override
	public Optional<ITypedIngredient<?>> getIngredientUnderMouse() {
		return Optional.empty();
	}

	@Nullable
	@Override
	public <T> T getIngredientUnderMouse(IIngredientType<T> ingredientType) {
		return null;
	}
}
