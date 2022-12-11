package mezz.jei.gui.config;

import mezz.jei.core.config.GiveMode;
import mezz.jei.core.config.IngredientSortStage;

import java.util.List;

public interface IClientConfig {
    int minRecipeGuiHeight = 175;
    int defaultRecipeGuiHeight = 350;
    boolean defaultCenterSearchBar = false;

	boolean isCenterSearchBarEnabled();

	boolean isLowMemorySlowSearchEnabled();

	boolean isCheatToHotbarUsingHotkeysEnabled();

	GiveMode getGiveMode();

	int getMaxRecipeGuiHeight();

	List<IngredientSortStage> getIngredientSorterStages();
}
