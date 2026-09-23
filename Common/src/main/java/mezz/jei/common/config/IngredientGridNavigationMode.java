package mezz.jei.common.config;

public enum IngredientGridNavigationMode {
	PAGED,
	SCROLLING;

	public boolean usesScrollbar() {
		return this != PAGED;
	}
}
