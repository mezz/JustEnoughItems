package mezz.jei.common.input;

public interface ICharTypedHandler {
	boolean hasKeyboardFocus();

	boolean onCharTyped(char codePoint, int modifiers);
}
