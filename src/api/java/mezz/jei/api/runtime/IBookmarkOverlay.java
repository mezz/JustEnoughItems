package mezz.jei.api.runtime;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * The {@link IBookmarkOverlay} is JEI's gui that displays all the bookmarked ingredients next to an open container gui.
 * Use this interface to get information from it.
 * Get the instance from {@link IJeiRuntime#getBookmarkOverlay()}.
 */
public interface IBookmarkOverlay {
	/**
	 * @return the ingredient that's currently under the mouse.
	 * @since JEI 9.3.0
	 */
	Optional<ITypedIngredient<?>> getIngredientUnderMouse();

	/**
	 * @return the ingredient that's currently under the mouse, or null if there is none.
	 */
	@Nullable
	<T> T getIngredientUnderMouse(IIngredientType<T> ingredientType);
}
