package mezz.jei.common.config;

import mezz.jei.common.gui.overlay.options.HorizontalAlignment;
import mezz.jei.common.gui.overlay.options.NavigationVisibility;
import mezz.jei.common.gui.overlay.options.VerticalAlignment;

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
