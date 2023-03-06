package mezz.jei.core.config;

import mezz.jei.core.util.gui.HorizontalAlignment;
import mezz.jei.core.util.gui.NavigationVisibility;
import mezz.jei.core.util.gui.VerticalAlignment;

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
