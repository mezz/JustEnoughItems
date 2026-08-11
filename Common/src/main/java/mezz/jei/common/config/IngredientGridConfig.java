package mezz.jei.common.config;

import mezz.jei.common.config.file.ConfigValue;
import mezz.jei.common.config.file.IConfigCategoryBuilder;
import mezz.jei.common.config.file.IConfigSchemaBuilder;
import mezz.jei.common.util.HorizontalAlignment;
import mezz.jei.common.util.NavigationVisibility;
import mezz.jei.common.util.VerticalAlignment;

public class IngredientGridConfig implements IIngredientGridConfig {
	private static final int minNumRows = 1;
	private static final int defaultNumRows = 16;
	private static final int largestNumRows = 100;

	private static final int minNumColumns = 4;
	private static final int defaultNumColumns = 9;
	private static final int largestNumColumns = 100;

	private static final VerticalAlignment defaultVerticalAlignment = VerticalAlignment.TOP;
	private static final NavigationVisibility defaultNavigationVisibility = NavigationVisibility.ENABLED;
	private static final IngredientGridLayoutMode defaultLayoutMode = IngredientGridLayoutMode.RECTANGULAR;
	private static final IngredientGridNavigationMode defaultNavigationMode = IngredientGridNavigationMode.PAGED;
	private static final boolean defaultDrawBackground = false;

	private final ConfigValue<Integer> maxRows;
	private final ConfigValue<Integer> maxColumns;
	private final ConfigValue<HorizontalAlignment> horizontalAlignment;
	private final ConfigValue<VerticalAlignment> verticalAlignment;
	private final ConfigValue<NavigationVisibility> navigationVisibility;
	private final ConfigValue<IngredientGridLayoutMode> layoutMode;
	private final ConfigValue<IngredientGridNavigationMode> navigationMode;
	private final ConfigValue<Boolean> drawBackground;

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
		navigationVisibility = category.addEnum(
			"NavigationVisibility",
			defaultNavigationVisibility,
			"Visibility of navigation controls like page buttons and scroll bars. Use AUTO_HIDE to only show them when the list overflows."
		);
		drawBackground = category.addBoolean(
			"DrawBackground",
			defaultDrawBackground,
			"Enable this to draw a background texture behind the GUI."
		);
		layoutMode = category.addEnum(
			"LayoutMode",
			defaultLayoutMode,
			"RECTANGULAR keeps page navigation aligned to the grid while still allowing excluded grid slots to be cut out. MAXIMIZE_AVAILABLE_SPACE can resize and shift page navigation around excluded areas."
		);
		navigationMode = category.addEnum(
			"NavigationMode",
			defaultNavigationMode,
			"Choose PAGED for page buttons, SCROLLING for a row-stepped scroll bar, or SMOOTH_SCROLLING for a smooth scroll bar."
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
	public IngredientGridLayoutMode getLayoutMode() {
		return layoutMode.get();
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
	public NavigationVisibility getNavigationVisibility() {
		return navigationVisibility.get();
	}

	@Override
	public IngredientGridNavigationMode getNavigationMode() {
		return navigationMode.get();
	}

	@Override
	public void addLayoutListener(Runnable listener) {
		maxRows.addListener(v -> listener.run());
		maxColumns.addListener(v -> listener.run());
		horizontalAlignment.addListener(v -> listener.run());
		verticalAlignment.addListener(v -> listener.run());
		navigationVisibility.addListener(v -> listener.run());
		layoutMode.addListener(v -> listener.run());
		navigationMode.addListener(v -> listener.run());
		drawBackground.addListener(v -> listener.run());
	}
}
