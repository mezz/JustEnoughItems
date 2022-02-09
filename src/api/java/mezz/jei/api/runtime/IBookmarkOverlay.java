package mezz.jei.api.runtime;

import mezz.jei.api.ingredients.IIngredientType;

import org.jetbrains.annotations.Nullable;

/**
 * The {@link IBookmarkOverlay} is JEI's gui that displays all the bookmarked ingredients next to an open container gui.
 * Use this interface to get information from it.
 * Get the instance from {@link IJeiRuntime#getBookmarkOverlay()}.
 */
public interface IBookmarkOverlay {
	/**
	 * @return the ingredient that's currently under the mouse, or null if there is none.
	 */
	@Nullable
	<T> T getIngredientUnderMouse(IIngredientType<T> ingredientType);
}
