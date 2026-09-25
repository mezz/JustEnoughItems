package mezz.jei.common.config;

public enum IngredientGridBackgroundStyle {
	NONE,
	BACKGROUND,
	BORDER_ONLY,
	GRID;

	public boolean isEnabled() {
		return this != NONE;
	}

	public static IngredientGridBackgroundStyle fromBoolean(boolean drawBackground) {
		if (drawBackground) {
			return BACKGROUND;
		}
		return NONE;
	}
}
