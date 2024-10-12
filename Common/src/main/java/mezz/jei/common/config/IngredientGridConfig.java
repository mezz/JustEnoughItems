package mezz.jei.common.config;

import mezz.jei.common.util.HorizontalAlignment;
import mezz.jei.common.util.NavigationVisibility;
import mezz.jei.common.util.VerticalAlignment;
import mezz.jei.common.config.file.IConfigCategoryBuilder;
import mezz.jei.common.config.file.IConfigSchemaBuilder;

import java.util.function.Supplier;

public class IngredientGridConfig implements IIngredientGridConfig {
	private static final int minNumRows = 1;
	private static final int defaultNumRows = 16;
	private static final int largestNumRows = 100;

	private static final int minNumColumns = 4;
	private static final int defaultNumColumns = 9;
	private static final int largestNumColumns = 100;

	private static final VerticalAlignment defaultVerticalAlignment = VerticalAlignment.TOP;
	private static final NavigationVisibility defaultButtonNavigationVisibility = NavigationVisibility.ENABLED;
	private static final boolean defaultDrawBackground = false;

	private final Supplier<Integer> maxRows;
	private final Supplier<Integer> maxColumns;
	private final Supplier<HorizontalAlignment> horizontalAlignment;
	private final Supplier<VerticalAlignment> verticalAlignment;
	private final Supplier<NavigationVisibility> buttonNavigationVisibility;
	private final Supplier<Boolean> drawBackground;

	public IngredientGridConfig(String categoryName, IConfigSchemaBuilder builder, HorizontalAlignment defaultHorizontalAlignment) {
		IConfigCategoryBuilder category = builder.addCategory(categoryName);
		maxRows = category.addInteger(
			"MaxRows",
			defaultNumRows,
			minNumRows,
			largestNumRows,
			"Max number of rows shown."
		);
		maxColumns = category.addInteger(
			"MaxColumns",
			defaultNumColumns,
			minNumColumns,
			largestNumColumns,
			"Max number of columns shown."
		);
		horizontalAlignment = category.addEnum(
			"HorizontalAlignment",
			defaultHorizontalAlignment,
			"Horizontal alignment of the ingredient grid inside the available area."
		);
		verticalAlignment = category.addEnum(
			"VerticalAlignment",
			defaultVerticalAlignment,
			"Vertical alignment of the ingredient grid inside the available area."
		);
		buttonNavigationVisibility = category.addEnum(
			"ButtonNavigationVisibility",
			defaultButtonNavigationVisibility,
			"Visibility of the top page buttons. Use AUTO_HIDE to only show it when there are multiple pages."
		);
		drawBackground = category.addBoolean(
			"DrawBackground",
			defaultDrawBackground,
			"Enable this to draw a background texture behind the GUI."
		);
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
