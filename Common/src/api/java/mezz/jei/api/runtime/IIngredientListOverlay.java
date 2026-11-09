package mezz.jei.api.runtime;

import mezz.jei.api.gui.buttons.ButtonSnapshot;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import org.jspecify.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.Optional;

/**
 * JEI's ingredient list beside an open container screen.
 * Obtain it from {@link IJeiRuntime#getIngredientListOverlay()}.
 *
 * Bounds use GUI-scaled screen coordinates.
 * Check visibility separately from bounds and enabled state.
 */
@ApiStatus.NonExtendable
public interface IIngredientListOverlay {
	/**
	 * @return the ingredient that's currently under the mouse.
	 * @since 9.3.0
	 */
	Optional<ITypedIngredient<?>> getIngredientUnderMouse();

	/**
	 * @return the ingredient that's currently under the mouse if it matches the given type, or null if there is none.
	 * @since 7.0.1
	 */
	@Nullable
	<T> T getIngredientUnderMouse(IIngredientType<T> ingredientType);

	/**
	 * @return true if the ingredient list is currently displayed.
	 *
	 * @since 10.1.0
	 */
	boolean isListDisplayed();

	/**
	 * Returns whether the overlay has room for ingredients, even when hidden.
	 * @since 31.22.0
	 */
	boolean hasRoom();

	/**
	 * Bounds of the ingredient panel.
	 * Returns empty bounds when no layout is available.
	 * @since 31.22.0
	 */
	ScreenRectangle getBounds();

	/**
	 * Bounds of the ingredient grid, excluding navigation controls.
	 * Returns empty bounds when no layout is available.
	 * @since 31.22.0
	 */
	ScreenRectangle getGridBounds();

	/**
	 * Zero-based page index, or zero when no layout is available.
	 * In scrollbar mode, this is the first visible row.
	 * @since 31.22.0
	 */
	int getPageIndex();

	/**
	 * Number of pages, or zero when no layout is available.
	 * In scrollbar mode, this is the number of scroll positions.
	 * @since 31.22.0
	 */
	int getPageCount();

	/**
	 * Returns a snapshot of the next-page button.
	 * @since 31.22.0
	 */
	ButtonSnapshot getNextButton();

	/**
	 * Returns a snapshot of the previous-page button.
	 * @since 31.22.0
	 */
	ButtonSnapshot getBackButton();

	/**
	 * @return true if the text box is focused by the player.
	 */
	boolean hasKeyboardFocus();

	/**
	 * @return a list containing all currently visible ingredients. If JEI is hidden, the list will be empty.
	 */
	<T> List<T> getVisibleIngredients(IIngredientType<T> ingredientType);
}
