package mezz.jei.gui.search;

public record Token(
		String text,
		boolean exclusion
) {
	public boolean isEmpty() {
		return text.isEmpty();
	}
}
