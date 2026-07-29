package mezz.jei.gui.search;

public record Token(
		String text,
		int startIndex,
		int endIndex,
		boolean quoted,
		boolean exclusion
) {
	public boolean isEmpty() {
		return text.isEmpty();
	}
}
