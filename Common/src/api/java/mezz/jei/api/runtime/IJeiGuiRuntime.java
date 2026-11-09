package mezz.jei.api.runtime;

import mezz.jei.api.gui.buttons.ButtonSnapshot;
import org.jetbrains.annotations.ApiStatus;

import java.util.Optional;

/**
 * Gives access to JEI's GUI components, ingredient filter, and bookmarks.
 * @since 31.22.0
 */
@ApiStatus.NonExtendable
public interface IJeiGuiRuntime {
	/**
	 * Returns the GUI for opening and viewing recipes.
	 * @since 31.22.0
	 */
	IRecipesGui getRecipesGui();

	/**
	 * Returns the overlay that displays ingredients beside container GUIs.
	 * @since 31.22.0
	 */
	IIngredientListOverlay getIngredientListOverlay();

	/**
	 * Returns the filter used to search ingredients.
	 * @since 31.22.0
	 */
	IIngredientFilter getIngredientFilter();

	/**
	 * Returns the overlay that displays bookmarked ingredients and recipes.
	 * @since 31.22.0
	 */
	IBookmarkOverlay getBookmarkOverlay();

	/**
	 * Returns access to saved ingredient and recipe bookmarks.
	 * @since 31.22.0
	 */
	IBookmarkManager getBookmarkManager();

	/**
	 * Returns a snapshot of the config button, or an empty result if the button does not exist.
	 * @since 31.22.0
	 */
	Optional<ButtonSnapshot> getConfigButton();

	/**
	 * Returns a snapshot of the bookmark toggle button, or an empty result if the button does not exist.
	 * @since 31.22.0
	 */
	Optional<ButtonSnapshot> getBookmarkToggleButton();
}
