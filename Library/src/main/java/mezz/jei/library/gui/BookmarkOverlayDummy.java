package mezz.jei.library.gui;

import mezz.jei.api.gui.buttons.ButtonSnapshot;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IBookmarkOverlay;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class BookmarkOverlayDummy implements IBookmarkOverlay {
	public static final IBookmarkOverlay INSTANCE = new BookmarkOverlayDummy();
	private static final ScreenRectangle EMPTY_BOUNDS = new ScreenRectangle(0, 0, 0, 0);
	private static final ButtonSnapshot EMPTY_BUTTON = new ButtonSnapshot(EMPTY_BOUNDS, false, false);

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

	@Override
	public boolean isListDisplayed() {
		return false;
	}

	@Override
	public boolean hasRoom() {
		return false;
	}

	@Override
	public ScreenRectangle getBounds() {
		return EMPTY_BOUNDS;
	}

	@Override
	public ScreenRectangle getGridBounds() {
		return EMPTY_BOUNDS;
	}

	@Override
	public int getPageIndex() {
		return 0;
	}

	@Override
	public int getPageCount() {
		return 0;
	}

	@Override
	public ButtonSnapshot getNextButton() {
		return EMPTY_BUTTON;
	}

	@Override
	public ButtonSnapshot getBackButton() {
		return EMPTY_BUTTON;
	}
}
