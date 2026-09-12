package mezz.jei.common.config;

public enum SearchBarPosition {
	STANDARD,
	CENTERED;

	public static SearchBarPosition fromCentered(boolean centered) {
		if (centered) {
			return CENTERED;
		}
		return STANDARD;
	}

	public boolean isCentered() {
		return this == CENTERED;
	}
}
