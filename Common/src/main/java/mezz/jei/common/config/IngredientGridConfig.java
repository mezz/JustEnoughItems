package mezz.jei.common.config;

import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.placement.VerticalAlignment;
import net.mezzdev.config.api.value.editor.ConfigValueEditMode;
import net.mezzdev.config.api.value.IConfigValue;
import net.mezzdev.config.api.schema.builder.IConfigCategoryBuilder;

public class IngredientGridConfig implements IIngredientGridConfig {
	private static final int minNumRows = 1;
	private static final int defaultNumRows = 16;
	private static final int largestNumRows = 100;

	private static final int minNumColumns = 2;
	private static final int defaultNumColumns = 9;
	private static final int largestNumColumns = 100;

	private static final VerticalAlignment defaultVerticalAlignment = VerticalAlignment.TOP;
	private static final NavigationVisibility defaultNavigationVisibility = NavigationVisibility.ENABLED;
	private static final boolean defaultDrawBackground = false;
	private static final IngredientGridLayoutMode defaultLayoutMode = IngredientGridLayoutMode.RECTANGULAR;
	private static final IngredientGridNavigationMode defaultNavigationMode = IngredientGridNavigationMode.PAGED;

	private final IConfigValue<Integer> maxRows;
	private final IConfigValue<Integer> maxColumns;
	private final IConfigValue<HorizontalAlignment> horizontalAlignment;
	private final IConfigValue<VerticalAlignment> verticalAlignment;
	private final IConfigValue<NavigationVisibility> navigationVisibility;
	private final IConfigValue<Boolean> drawBackground;
	private final IConfigValue<IngredientGridLayoutMode> layoutMode;
	private final IConfigValue<IngredientGridNavigationMode> navigationMode;

	public IngredientGridConfig(IConfigCategoryBuilder category, HorizontalAlignment defaultHorizontalAlignment) {
		maxRows = category.addInteger("maxRows", defaultNumRows, minNumRows, largestNumRows)
			.setEditMode(ConfigValueEditMode.IMMEDIATE)
			.build();
		maxColumns = category.addInteger("maxColumns", defaultNumColumns, minNumColumns, largestNumColumns)
			.setEditMode(ConfigValueEditMode.IMMEDIATE)
			.build();
		horizontalAlignment = category.addEnum("horizontalAlignment", defaultHorizontalAlignment)
			.setEditMode(ConfigValueEditMode.IMMEDIATE)
			.build();
		verticalAlignment = category.addEnum("verticalAlignment", defaultVerticalAlignment)
			.setEditMode(ConfigValueEditMode.IMMEDIATE)
			.build();
		navigationVisibility = category.addEnum("navigationVisibility", defaultNavigationVisibility)
			.addLegacyName("buttonNavigationVisibility")
			.setEditMode(ConfigValueEditMode.IMMEDIATE)
			.build();
		drawBackground = category.addBoolean("drawBackground", defaultDrawBackground)
			.setEditMode(ConfigValueEditMode.IMMEDIATE)
			.build();
		layoutMode = category.addEnum("layoutMode", defaultLayoutMode)
			.setEditMode(ConfigValueEditMode.IMMEDIATE)
			.build();
		navigationMode = category.addEnum("navigationMode", defaultNavigationMode)
			.setEditMode(ConfigValueEditMode.IMMEDIATE)
			.build();
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
	public IConfigValue<Integer> maxColumns() {
		return maxColumns;
	}

	@Override
	public IConfigValue<Integer> maxRows() {
		return maxRows;
	}

	@Override
	public IConfigValue<Boolean> drawBackground() {
		return drawBackground;
	}

	@Override
	public IConfigValue<IngredientGridLayoutMode> layoutMode() {
		return layoutMode;
	}

	@Override
	public IConfigValue<IngredientGridNavigationMode> navigationMode() {
		return navigationMode;
	}

	@Override
	public IConfigValue<HorizontalAlignment> horizontalAlignment() {
		return horizontalAlignment;
	}

	@Override
	public IConfigValue<VerticalAlignment> verticalAlignment() {
		return verticalAlignment;
	}

	@Override
	public IConfigValue<NavigationVisibility> navigationVisibility() {
		return navigationVisibility;
	}
}
