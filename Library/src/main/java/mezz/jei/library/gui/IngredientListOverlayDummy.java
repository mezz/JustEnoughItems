package mezz.jei.library.gui;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientListOverlay;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class IngredientListOverlayDummy implements IIngredientListOverlay{
	public static final IIngredientListOverlay INSTANCE = new IngredientListOverlayDummy();

	private IngredientListOverlayDummy() {

	}

	@Override
	public boolean isListDisplayed() {
		return false;
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
