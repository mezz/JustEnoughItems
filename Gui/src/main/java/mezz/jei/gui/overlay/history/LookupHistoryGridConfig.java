package mezz.jei.gui.overlay.history;

import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.placement.VerticalAlignment;
import mezz.jei.common.config.IIngredientGridConfig;
import mezz.jei.common.config.IngredientGridLayoutMode;
import mezz.jei.common.config.IngredientGridNavigationMode;
import mezz.jei.common.config.NavigationVisibility;
import net.mezzdev.config.api.value.IConfigValue;

public final class LookupHistoryGridConfig implements IIngredientGridConfig {
	private final IIngredientGridConfig ownerConfig;
	private final IConfigValue<Integer> maxRows;

	public LookupHistoryGridConfig(IIngredientGridConfig ownerConfig, IConfigValue<Integer> maxRows) {
		this.ownerConfig = ownerConfig;
		this.maxRows = maxRows;
	}

	@Override
	public IConfigValue<Integer> maxColumns() {
		return ownerConfig.maxColumns();
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
	public IConfigValue<Boolean> drawBackground() {
		return ownerConfig.drawBackground();
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
