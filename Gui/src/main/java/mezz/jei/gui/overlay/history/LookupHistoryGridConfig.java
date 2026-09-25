package mezz.jei.gui.overlay.history;

import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.placement.VerticalAlignment;
import mezz.jei.common.config.IIngredientGridConfig;
import mezz.jei.common.config.IngredientGridBackgroundStyle;
import mezz.jei.common.config.IngredientGridLayoutMode;
import mezz.jei.common.config.IngredientGridNavigationMode;
import mezz.jei.common.config.NavigationVisibility;
import net.mezzdev.config.api.value.IConfigValue;

public final class LookupHistoryGridConfig implements IIngredientGridConfig {
	private final IIngredientGridConfig ownerConfig;
	private final IConfigValue<Integer> maxRows;
	private final IConfigValue<Integer> maxColumns;

	public LookupHistoryGridConfig(IIngredientGridConfig ownerConfig, IConfigValue<Integer> maxRows, IConfigValue<Integer> maxColumns) {
		this.ownerConfig = ownerConfig;
		this.maxRows = maxRows;
		this.maxColumns = maxColumns;
	}

	@Override
	public IConfigValue<Integer> maxColumns() {
		return maxColumns;
	}

	@Override
	public int getMinColumns() {
		return ownerConfig.getMinColumns();
	}

	@Override
	public IConfigValue<Integer> maxRows() {
		return maxRows;
	}

	@Override
	public int getMinRows() {
		return ownerConfig.getMinRows();
	}

	@Override
	public IConfigValue<IngredientGridBackgroundStyle> backgroundStyle() {
		return ownerConfig.backgroundStyle();
	}

	@Override
	public IConfigValue<IngredientGridLayoutMode> layoutMode() {
		return ownerConfig.layoutMode();
	}

	@Override
	public IConfigValue<HorizontalAlignment> horizontalAlignment() {
		return ownerConfig.horizontalAlignment();
	}

	@Override
	public IConfigValue<VerticalAlignment> verticalAlignment() {
		return ownerConfig.verticalAlignment();
	}

	@Override
	public IConfigValue<NavigationVisibility> navigationVisibility() {
		return ownerConfig.navigationVisibility();
	}

	@Override
	public IConfigValue<IngredientGridNavigationMode> navigationMode() {
		return ownerConfig.navigationMode();
	}
}
