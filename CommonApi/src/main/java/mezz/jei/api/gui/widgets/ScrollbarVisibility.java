package mezz.jei.api.gui.widgets;

/**
 * Controls when a recipe widget displays its scrollbar. Hidden scrollbars still allow mouse-wheel scrolling.
 *
 * @since 15.69.0
 */
public enum ScrollbarVisibility {
	/** Follow JEI's shared ingredient navigation visibility setting. */
	DEFAULT,
	/** Always show the scrollbar, including when all contents fit. */
	ENABLED,
	/** Show the scrollbar only when some contents are outside the visible area. */
	AUTO_HIDE,
	/** Hide the scrollbar. */
	DISABLED
}
