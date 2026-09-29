package mezz.jei.common.input.interaction;

import mezz.jei.common.input.UserInput;

/**
 * An interaction that marks input as handled, with nothing left to do on release or cancellation.
 * Used when pressing the key already did the work, such as editing text, or when a
 * visible input area must prevent a click from reaching the Minecraft screen behind it.
 */
public enum ConsumedInput implements IInputInteraction {
	/** Stops the input from reaching other controls. */
	INSTANCE;

	@Override
	public void complete(UserInput input) {
	}
}
