package mezz.jei.common.config;

import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.placement.VerticalAlignment;
import mezz.jei.common.util.NavigationVisibility;
import mezz.jei.common.config.file.IConfigCategoryBuilder;
import mezz.jei.common.config.file.IConfigSchemaBuilder;

import java.util.function.Supplier;

public class IngredientGridConfig implements IIngredientGridConfig {
	private static final int minNumRows = 1;
	private static final int defaultNumRows = 16;
	private static final int largestNumRows = 100;

	private static final int minNumColumns = 2;
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
			"maxRows",
			defaultNumRows,
			minNumRows,
			largestNumRows
		);
		maxColumns = category.addInteger(
			"maxColumns",
			defaultNumColumns,
			minNumColumns,
			largestNumColumns
		);
		horizontalAlignment = category.addEnum("horizontalAlignment", defaultHorizontalAlignment);
		verticalAlignment = category.addEnum("verticalAlignment", defaultVerticalAlignment);
		buttonNavigationVisibility = category.addEnum("buttonNavigationVisibility", defaultButtonNavigationVisibility);
		drawBackground = category.addBoolean("drawBackground", defaultDrawBackground);
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
