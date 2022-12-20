package mezz.jei.gui.config;

import mezz.jei.gui.util.HorizontalAlignment;
import mezz.jei.gui.overlay.options.NavigationVisibility;
import mezz.jei.gui.util.VerticalAlignment;

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
