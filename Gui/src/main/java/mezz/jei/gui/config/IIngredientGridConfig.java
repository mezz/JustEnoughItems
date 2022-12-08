package mezz.jei.gui.config;

import mezz.jei.common.gui.overlay.HorizontalAlignment;
import mezz.jei.gui.overlay.options.NavigationVisibility;
import mezz.jei.common.gui.overlay.VerticalAlignment;

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
