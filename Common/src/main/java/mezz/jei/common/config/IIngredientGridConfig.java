package mezz.jei.common.config;

import mezz.jei.common.util.HorizontalAlignment;
import mezz.jei.common.util.NavigationVisibility;
import mezz.jei.common.util.VerticalAlignment;

public interface IIngredientGridConfig {
	int getMaxColumns();
	int getMinColumns();
	int getMaxRows();
	int getMinRows();
	boolean drawBackground();
	HorizontalAlignment getHorizontalAlignment();
	VerticalAlignment getVerticalAlignment();
	NavigationVisibility getButtonNavigationVisibility();
}
