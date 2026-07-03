package mezz.jei.gui.overlay;

import mezz.jei.gui.overlay.elements.IElement;
import org.jspecify.annotations.Nullable;

/**
 * Page navigation for an ingredient grid, including anchors for keeping an element visible across layout changes.
 */
public interface IIngredientGridPageNavigation {
	/**
	 * Returns an ingredient element that should remain visible across the next layout update, if one exists.
	 */
	@Nullable
	IElement<?> getPageAnchorElement();

	/**
	 * Rebuilds the current page so the given anchor element stays visible when possible.
	 */
	void updateLayoutKeepingPageAnchorVisible(@Nullable IElement<?> pageAnchorElement);

	/**
	 * Rebuilds the grid starting from the first page.
	 */
	void updateLayoutToFirstPage();
}
