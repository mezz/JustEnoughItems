package mezz.jei.gui.overlay;

import mezz.jei.common.util.ImmutablePoint2i;
import mezz.jei.common.util.ImmutableRect2i;
import org.jspecify.annotations.Nullable;

import java.util.Set;

/**
 * A positioned ingredient grid that can be laid out and closed as the screen state changes.
 */
public interface IIngredientGridView {
	/**
	 * Returns true when the grid has enough space to render ingredients.
	 */
	boolean hasRoom();

	/**
	 * Closes the grid and clears any screen-specific state.
	 */
	void close();

	/**
	 * Updates the grid bounds using the available overlay area and screen exclusion regions.
	 */
	void updateBounds(ImmutableRect2i availableArea, Set<ImmutableRect2i> guiExclusionAreas, @Nullable ImmutablePoint2i mouseExclusionPoint);

	/**
	 * Returns the rendered background area used to align nearby overlay controls.
	 */
	ImmutableRect2i getBackgroundArea();
}
