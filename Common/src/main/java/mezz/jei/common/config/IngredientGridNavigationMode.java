package mezz.jei.common.config;

public enum IngredientGridNavigationMode {
	PAGED,
	SCROLLING,
	SMOOTH_SCROLLING;

	public boolean usesScrollbar() {
		return this != PAGED;
	}

	public boolean usesSmoothScrolling() {
		return this == SMOOTH_SCROLLING;
	}
}
