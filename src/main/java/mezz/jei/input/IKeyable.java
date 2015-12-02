package mezz.jei.input;

public interface IKeyable extends ICloseable {

	/**
	 * Returns true if all keyboard input should go to this IKeyable.
	 */
	boolean hasKeyboardFocus();

	void setKeyboardFocus(boolean keyboardFocus);

	/**
	 * Returns true if the key press was used.
	 */
	boolean onKeyPressed(int keyCode);

}
