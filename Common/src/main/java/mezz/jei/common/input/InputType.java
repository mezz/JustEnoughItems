package mezz.jei.common.input;

public enum InputType {
	/** called on mouse-down or to see if a click would be handled */
	SIMULATE,
	/** called on mouse-up after a successful {@link InputType#SIMULATE} */
	EXECUTE,
	/** called on key-down, or mouse-down to execute a click from a vanilla GUI without waiting for mouse-up */
	IMMEDIATE,
}
