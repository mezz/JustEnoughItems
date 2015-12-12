package mezz.jei.api.gui;

/**
 * A timer to help render things that normally depend on ticks.
 * Get an instance from the IGuiHelper
 */
public interface ITickTimer {
	int getValue();

	int getMaxValue();
}
