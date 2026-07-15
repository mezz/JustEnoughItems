package mezz.jei.common.config;

import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.placement.VerticalAlignment;
import mezz.jei.api.runtime.config.IJeiConfigValue;
import mezz.jei.common.util.NavigationVisibility;

public interface IIngredientGridConfig {
	IJeiConfigValue<Integer> maxColumns();

	int getMinColumns();

	IJeiConfigValue<Integer> maxRows();

	int getMinRows();

	IJeiConfigValue<Boolean> drawBackground();

	IJeiConfigValue<IngredientGridLayoutMode> layoutMode();

	IJeiConfigValue<IngredientGridNavigationMode> navigationMode();

	IJeiConfigValue<HorizontalAlignment> horizontalAlignment();

	IJeiConfigValue<VerticalAlignment> verticalAlignment();

	IJeiConfigValue<NavigationVisibility> navigationVisibility();
}
