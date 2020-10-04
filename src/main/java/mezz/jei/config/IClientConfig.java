package mezz.jei.config;

import mezz.jei.util.GiveMode;

public interface IClientConfig {
	boolean isDebugModeEnabled();

	boolean isCenterSearchBarEnabled();

	boolean isLowMemorySlowSearchEnabled();

	GiveMode getGiveMode();

	int getMaxColumns();

	int getMaxRecipeGuiHeight();
}
