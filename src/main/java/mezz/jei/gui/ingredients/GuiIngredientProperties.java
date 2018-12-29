package mezz.jei.gui.ingredients;

public final class GuiIngredientProperties {
	private static final int baseWidth = 16;
	private static final int baseHeight = 16;

	public static int getWidth(int padding) {
		return baseWidth + (2 * padding);
	}

	public static int getHeight(int padding) {
		return baseHeight + (2 * padding);
	}
}
