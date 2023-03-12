package mezz.jei.api.config;

import mezz.jei.api.config.gui.HorizontalAlignment;
import mezz.jei.api.config.gui.NavigationVisibility;
import mezz.jei.api.config.gui.VerticalAlignment;

public interface IIngredientGridConfig {
	 int minNumRows = 1;
	 int defaultNumRows = 48;
	 int largestNumRows = 100;

	 int minNumColumns = 4;
	 int defaultNumColumns = 9;
	 int largestNumColumns = 100;

	 VerticalAlignment defaultVerticalAlignment = VerticalAlignment.TOP;
	 NavigationVisibility defaultButtonNavigationVisibility = NavigationVisibility.ENABLED;
	 boolean defaultDrawBackground = false;

	int getMaxColumns();
	int getMinColumns();
	int getMaxRows();
	int getMinRows();
	boolean drawBackground();
	HorizontalAlignment getHorizontalAlignment();
	VerticalAlignment getVerticalAlignment();
	NavigationVisibility getButtonNavigationVisibility();
}
