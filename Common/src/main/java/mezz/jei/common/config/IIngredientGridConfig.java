package mezz.jei.common.config;

import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.placement.VerticalAlignment;
import net.mezzdev.config.api.value.IConfigValue;

public interface IIngredientGridConfig {
	IConfigValue<Integer> maxColumns();

	int getMinColumns();

	IConfigValue<Integer> maxRows();

	int getMinRows();

	IConfigValue<Boolean> drawBackground();

	IConfigValue<IngredientGridLayoutMode> layoutMode();

	IConfigValue<HorizontalAlignment> horizontalAlignment();

	IConfigValue<VerticalAlignment> verticalAlignment();

	IConfigValue<NavigationVisibility> navigationVisibility();

	IConfigValue<IngredientGridNavigationMode> navigationMode();
}
