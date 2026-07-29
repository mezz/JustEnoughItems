package mezz.jei.ingredients;

final class SearchToken {
	private final String text;
	private final boolean exclusion;

	SearchToken(String text, boolean exclusion) {
		this.text = text;
		this.exclusion = exclusion;
	}

	String getText() {
		return text;
	}

	boolean isExclusion() {
		return exclusion;
	}
}
