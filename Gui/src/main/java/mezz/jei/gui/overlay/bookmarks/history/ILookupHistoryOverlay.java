package mezz.jei.gui.overlay.bookmarks.history;

import mezz.jei.common.util.ImmutablePoint2i;
import mezz.jei.common.util.ImmutableRect2i;
import org.jspecify.annotations.Nullable;

import java.util.Set;

/**
 * A lookup-history panel that can be laid out, refreshed, and closed.
 */
public interface ILookupHistoryOverlay {
	/**
	 * Returns true when lookup history is configured to display on this overlay side.
	 * When false, lookup history is displayed by the overlay on the opposite side.
	 */
	boolean isDisplayedOnThisSide();

	/**
	 * Closes the lookup-history panel and clears any screen-specific state.
	 */
	void close();

	/**
	 * Updates the lookup-history bounds using the available overlay area and screen exclusion regions.
	 */
	void updateBounds(ImmutableRect2i availableArea, Set<ImmutableRect2i> guiExclusionAreas, @Nullable ImmutablePoint2i mouseExclusionPoint);

	/**
	 * Rebuilds the lookup-history layout using its current bounds.
	 */
	void updateLayout();
}
