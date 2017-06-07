package mezz.jei.api;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

/**
 * The IItemListOverlay is JEI's gui that displays all the ingredients next to an open container gui.
 * Use this interface to get information from and interact with it.
 * Get the instance from {@link IJeiRuntime#getIngredientListOverlay()}.
 *
 * @since JEI 4.5.0
 */
public interface IIngredientListOverlay {
	/**
	 * @return the ingredient that's currently under the mouse, or null if there is none.
	 */
	@Nullable
	Object getIngredientUnderMouse();

	/**
	 * @return true if the text box is focused by the player.
	 */
	boolean hasKeyboardFocus();

	/**
	 * @return a list containing all currently visible ingredients. If JEI is hidden, the list will be empty.
	 */
	ImmutableList<Object> getVisibleIngredients();
}
