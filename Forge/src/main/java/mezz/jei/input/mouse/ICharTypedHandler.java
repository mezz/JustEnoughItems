package mezz.jei.input.mouse;

public interface ICharTypedHandler {
	boolean hasKeyboardFocus();

	boolean onCharTyped(char codePoint, int modifiers);
}
