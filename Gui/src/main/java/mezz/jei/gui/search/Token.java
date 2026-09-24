package mezz.jei.gui.search;

public record Token(
	String text,
	boolean exclusion,
	boolean operator,
	int start,
	int end
) {
	public boolean isEmpty() {
		return text.isEmpty();
	}

	public static Token operator(int start) {
		return new Token("|", false, true, start, start + 1);
	}
}
