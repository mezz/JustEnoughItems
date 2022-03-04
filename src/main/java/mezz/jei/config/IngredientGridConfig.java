package mezz.jei.config;

import mezz.jei.gui.overlay.HorizontalAlignment;
import mezz.jei.gui.overlay.NavigationVisibility;
import mezz.jei.gui.overlay.VerticalAlignment;
import net.minecraftforge.common.ForgeConfigSpec;

public class IngredientGridConfig implements IIngredientGridConfig {
	private static final int minNumRows = 1;
	private static final int defaultNumRows = 16;
	private static final int largestNumRows = 100;

	private static final int minNumColumns = 4;
	private static final int defaultNumColumns = 9;
	private static final int largestNumColumns = 100;

	private final ForgeConfigSpec.IntValue maxRows;
	private final ForgeConfigSpec.IntValue maxColumns;
	private final ForgeConfigSpec.EnumValue<HorizontalAlignment> horizontalAlignment;
	private final ForgeConfigSpec.EnumValue<VerticalAlignment> verticalAlignment;
	private final ForgeConfigSpec.EnumValue<NavigationVisibility> buttonNavigationVisibility;
	private final ForgeConfigSpec.BooleanValue drawBackground;

	public IngredientGridConfig(String categoryName, ForgeConfigSpec.Builder builder, HorizontalAlignment defaultHorizontalAlignment) {
		builder.push(categoryName);
		{
			builder.comment("Max number of rows shown");
			maxRows = builder.defineInRange("MaxRows", defaultNumRows, minNumRows, largestNumRows);

			builder.comment("Max number of columns shown");
			maxColumns = builder.defineInRange("MaxColumns", defaultNumColumns, minNumColumns, largestNumColumns);

			builder.comment("Horizontal alignment of the ingredient grid inside the available area");
			horizontalAlignment = builder.defineEnum("HorizontalAlignment", defaultHorizontalAlignment);

			builder.comment("Horizontal alignment of the ingredient grid inside the available area");
			verticalAlignment = builder.defineEnum("VerticalAlignment", VerticalAlignment.TOP);

			builder.comment("Visibility of the top page buttons. Use AUTO_HIDE to only show it when there are multiple pages.");
			buttonNavigationVisibility = builder.defineEnum("ButtonNavigationVisibility", NavigationVisibility.ENABLED);

			builder.comment("Set to true to draw a background texture behind the gui.");
			drawBackground = builder.define("DrawBackground", false);
		}
		builder.pop();
	}

	@Override
	public int getMinColumns() {
		return minNumColumns;
	}

	@Override
	public int getMinRows() {
		return minNumRows;
	}

	@Override
	public HorizontalAlignment getHorizontalAlignment() {
		return horizontalAlignment.get();
	}

	@Override
	public VerticalAlignment getVerticalAlignment() {
		return verticalAlignment.get();
	}

	@Override
	public boolean drawBackground() {
		return drawBackground.get();
	}

	@Override
	public int getMaxColumns() {
		return maxColumns.get();
	}

	@Override
	public int getMaxRows() {
		return maxRows.get();
	}

	@Override
	public NavigationVisibility getButtonNavigationVisibility() {
		return buttonNavigationVisibility.get();
	}
}
