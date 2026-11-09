package mezz.jei.library.gui;

import mezz.jei.api.gui.buttons.ButtonSnapshot;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientListOverlay;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class IngredientListOverlayDummy implements IIngredientListOverlay {
	public static final IIngredientListOverlay INSTANCE = new IngredientListOverlayDummy();
	private static final ScreenRectangle EMPTY_BOUNDS = new ScreenRectangle(0, 0, 0, 0);
	private static final ButtonSnapshot EMPTY_BUTTON = new ButtonSnapshot(EMPTY_BOUNDS, false, false);

	private IngredientListOverlayDummy() {

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

	@Override
	public boolean hasKeyboardFocus() {
		return false;
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
	public <T> List<T> getVisibleIngredients(IIngredientType<T> ingredientType) {
		return Collections.emptyList();
	}
}
