package mezz.jei.common.input.interaction;

import mezz.jei.common.input.UserInput;

/**
 * Marks input as handled without an action to run on release.
 * Text input reserves the key while allowing its character callbacks to insert text.
 */
public enum ConsumedInput implements IInputInteraction {
	/** Consumes the input and its character callbacks. */
	INSTANCE,
	/** Stops key handling by other controls while allowing character callbacks to insert text. */
	TEXT_INPUT {
		@Override
		public boolean consumesCharacters() {
			return false;
		}
	};

	@Override
	public void complete(UserInput input) {
	}
}
