package mezz.jei.gui.overlay;

import mezz.jei.common.util.ImmutableRect2i;

/**
 * A configuration button whose bounds are controlled by layout code.
 */
public interface IConfigButton {
	/**
	 * Updates the button bounds for the current overlay layout.
	 */
	void updateBounds(ImmutableRect2i area);
}
