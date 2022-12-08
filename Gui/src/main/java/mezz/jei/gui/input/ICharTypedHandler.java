package mezz.jei.gui.input;

public interface ICharTypedHandler {
	boolean hasKeyboardFocus();

	boolean onCharTyped(char codePoint, int modifiers);
}
