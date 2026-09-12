package mezz.jei.common.config;

import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.placement.VerticalAlignment;
import mezz.jei.common.Internal;
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

	default int getMaxColumns() {
		return maxColumns().get();
	}

	default int getMaxRows() {
		return maxRows().get();
	}

	default boolean isBackgroundDrawn() {
		return drawBackground().get();
	}

	default HorizontalAlignment getHorizontalAlignment() {
		return horizontalAlignment().get();
	}

	default VerticalAlignment getVerticalAlignment() {
		return verticalAlignment().get();
	}

	default NavigationVisibility getNavigationVisibility() {
		return navigationVisibility().get();
	}

	default IngredientGridLayoutMode getLayoutMode() {
		return layoutMode().get();
	}

	default IngredientGridNavigationMode getNavigationMode() {
		return navigationMode().get();
	}

	default void addLayoutListener(Runnable listener) {
		Internal.registerRuntimeListenerRemoval(maxColumns().addListener(change -> listener.run()));
		Internal.registerRuntimeListenerRemoval(maxRows().addListener(change -> listener.run()));
		Internal.registerRuntimeListenerRemoval(drawBackground().addListener(change -> listener.run()));
		Internal.registerRuntimeListenerRemoval(horizontalAlignment().addListener(change -> listener.run()));
		Internal.registerRuntimeListenerRemoval(verticalAlignment().addListener(change -> listener.run()));
		Internal.registerRuntimeListenerRemoval(navigationVisibility().addListener(change -> listener.run()));
	}
}
