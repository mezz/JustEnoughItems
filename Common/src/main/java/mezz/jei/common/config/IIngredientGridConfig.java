package mezz.jei.common.config;

import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.common.util.NavigationVisibility;
import mezz.jei.api.gui.placement.VerticalAlignment;

public interface IIngredientGridConfig {
	int getMaxColumns();
	int getMinColumns();
	int getMaxRows();
	int getMinRows();
	boolean drawBackground();
	HorizontalAlignment getHorizontalAlignment();
	VerticalAlignment getVerticalAlignment();
	NavigationVisibility getNavigationVisibility();
	IngredientGridLayoutMode getLayoutMode();
	IngredientGridNavigationMode getNavigationMode();

	void addLayoutListener(Runnable listener);
}
