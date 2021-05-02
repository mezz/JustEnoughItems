package mezz.jei.input.click;

public enum MouseClickState {
	/** called on mouse-down or to see if a click would be handled */
	SIMULATE,
	/** called on mouse-up after a successful {@link MouseClickState#SIMULATE} */
	EXECUTE,
	/** called on mouse-down to execute a click from a vanilla GUI, without waiting for mouse-up */
	VANILLA;

	public boolean isSimulate() {
		return this == SIMULATE;
	}

	public boolean isVanilla() {
		return this == VANILLA;
	}
}
