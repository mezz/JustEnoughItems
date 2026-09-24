package mezz.jei.gui.overlay;

import mezz.jei.common.util.ImmutableRect2i;

/**
 * A search text field with externally controlled value, focus state, and bounds.
 */
public interface ISearchField {
	/**
	 * Updates the displayed search text.
	 */
	void setValue(String filterText);

	/**
	 * Sets whether the search field has keyboard focus.
	 */
	void setFocused(boolean focused);

	/**
	 * Updates the search field bounds for the current overlay layout.
	 */
	void updateBounds(ImmutableRect2i area);
}
