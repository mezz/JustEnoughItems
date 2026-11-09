package mezz.jei.api.runtime;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.buttons.ButtonSnapshot;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;

import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import java.util.Optional;

/**
 * JEI's bookmarked ingredients beside an open container screen.
 * Obtain it from {@link IJeiRuntime#getBookmarkOverlay()}.
 *
 * Bounds use GUI-scaled screen coordinates.
 * Check visibility separately from bounds and enabled state.
 */
@ApiStatus.NonExtendable
public interface IBookmarkOverlay {
	/**
	 * @return the ingredient that's currently under the mouse.
	 * @since 9.3.0
	 */
	Optional<ITypedIngredient<?>> getIngredientUnderMouse();

	/**
	 * @return the ingredient that's currently under the mouse, or null if there is none.
	 */
	@Nullable
	<T> T getIngredientUnderMouse(IIngredientType<T> ingredientType);

	/**
	 * @return the ingredient that's currently under the mouse, or null if there is none.
	 */
	@Nullable
	default ItemStack getItemStackUnderMouse() {
		return getIngredientUnderMouse(VanillaTypes.ITEM_STACK);
	}

	/**
	 * Returns whether the bookmark list is currently displayed.
	 * @since 19.75.0
	 */
	boolean isListDisplayed();

	/**
	 * Returns whether the overlay has room for ingredients, even when hidden.
	 * @since 19.75.0
	 */
	boolean hasRoom();

	/**
	 * Bounds of the ingredient panel.
	 * Returns empty bounds when no layout is available.
	 * @since 19.75.0
	 */
	ScreenRectangle getBounds();

	/**
	 * Bounds of the ingredient grid, excluding navigation controls.
	 * Returns empty bounds when no layout is available.
	 * @since 19.75.0
	 */
	ScreenRectangle getGridBounds();

	/**
	 * Zero-based page index, or zero when no layout is available.
	 * In scrollbar mode, this is the first visible row.
	 * @since 19.75.0
	 */
	int getPageIndex();

	/**
	 * Number of pages, or zero when no layout is available.
	 * In scrollbar mode, this is the number of scroll positions.
	 * @since 19.75.0
	 */
	int getPageCount();

	/**
	 * Returns a snapshot of the next-page button.
	 * @since 19.75.0
	 */
	ButtonSnapshot getNextButton();

	/**
	 * Returns a snapshot of the previous-page button.
	 * @since 19.75.0
	 */
	ButtonSnapshot getBackButton();
}
