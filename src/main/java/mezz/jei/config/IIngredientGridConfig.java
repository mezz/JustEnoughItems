package mezz.jei.config;

import mezz.jei.gui.overlay.BackgroundType;
import mezz.jei.gui.overlay.HorizontalAlignment;
import mezz.jei.gui.overlay.NavigationVisibility;
import mezz.jei.gui.overlay.VerticalAlignment;
import mezz.jei.util.ImmutableRect2i;

public interface IIngredientGridConfig {
	int getMaxColumns();
	int getMinColumns();
	int getMaxRows();
	int getMinRows();
	BackgroundType getBackgroundType();
	HorizontalAlignment getHorizontalAlignment();
	VerticalAlignment getVerticalAlignment();
	NavigationVisibility getButtonNavigationVisibility();

	ImmutableRect2i calculateBounds(ImmutableRect2i availableArea, int ingredientWidth, int ingredientHeight);
}
