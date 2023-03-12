package mezz.jei.fabric.config;

import mezz.jei.api.config.IIngredientGridConfig;
import mezz.jei.api.config.gui.HorizontalAlignment;
import mezz.jei.api.config.gui.NavigationVisibility;
import mezz.jei.api.config.gui.VerticalAlignment;
import mezz.jei.core.config.file.IConfigCategoryBuilder;
import mezz.jei.core.config.file.IConfigSchemaBuilder;

import java.util.function.Supplier;

public class IngredientGridConfig implements IIngredientGridConfig {
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
			"Max number of rows shown"
		);
		maxColumns = category.addInteger(
			"MaxColumns",
			defaultNumColumns,
			minNumColumns,
			largestNumColumns,
			"Max number of columns shown"
		);
		horizontalAlignment = category.addEnum(
			"HorizontalAlignment",
			defaultHorizontalAlignment,
			"Horizontal alignment of the ingredient grid inside the available area"
		);
		verticalAlignment = category.addEnum(
			"VerticalAlignment",
			defaultVerticalAlignment,
			"Vertical alignment of the ingredient grid inside the available area"
		);
		buttonNavigationVisibility = category.addEnum(
			"ButtonNavigationVisibility",
			defaultButtonNavigationVisibility,
			"Visibility of the top page buttons. Use AUTO_HIDE to only show it when there are multiple pages."
		);
		drawBackground = category.addBoolean(
			"DrawBackground",
			defaultDrawBackground,
			"Set to true to draw a background texture behind the gui."
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
