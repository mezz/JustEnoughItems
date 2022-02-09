package mezz.jei.api.runtime;

import org.jetbrains.annotations.Nullable;

import mezz.jei.api.ingredients.IIngredientType;

import java.util.List;

/**
 * The IItemListOverlay is JEI's gui that displays all the ingredients next to an open container gui.
 * Use this interface to get information from and interact with it.
 * Get the instance from {@link IJeiRuntime#getIngredientListOverlay()}.
 */
public interface IIngredientListOverlay {
	/**
	 * @return the ingredient that's currently under the mouse if it matches the given type, or null if there is none.
	 * @since JEI 7.0.1
	 */
	@Nullable
	<T> T getIngredientUnderMouse(IIngredientType<T> ingredientType);

	/**
	 * @return true if the text box is focused by the player.
	 */
	boolean hasKeyboardFocus();

	/**
	 * @return a list containing all currently visible ingredients. If JEI is hidden, the list will be empty.
	 */
	<T> List<T> getVisibleIngredients(IIngredientType<T> ingredientType);
}
