package mezz.jei.util;

public class NullItemInStackException extends NullPointerException {
	/**
	 * Constructs a {@code NullItemInStackException} with no detail message.
	 */
	public NullItemInStackException() {
		super("Found itemStack with a null item. This is an error from another mod.");
	}

	/**
	 * Constructs a {@code NullItemInStackException} with the specified
	 * detail message.
	 *
	 * @param s the detail message.
	 */
	public NullItemInStackException(String s) {
		super(s);
	}
}
