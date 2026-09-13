package mezz.jei.gui.input;

/**
 * Owns a tooltip that stays pinned while the pin key is held.
 * The {@link PinnedTooltipManager} ensures that only one is shown at a time.
 */
public interface IPinnedTooltipHolder {
	void hide();
}
