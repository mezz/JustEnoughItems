package mezz.jei.core.config;

import mezz.jei.core.config.GiveMode;
import mezz.jei.core.config.IngredientSortStage;

import java.util.List;
import java.util.stream.Stream;

public interface IClientConfig {
    int minRecipeGuiHeight = 175;
    int defaultRecipeGuiHeight = 350;
    boolean defaultCenterSearchBar = false;

	boolean isCenterSearchBarEnabled();

	boolean isLowMemorySlowSearchEnabled();

	boolean isCheatToHotbarUsingHotkeysEnabled();

	boolean isAddingBookmarksToFront();

	GiveMode getGiveMode();

	int getMaxRecipeGuiHeight();

	Stream<IngredientSortStage> getIngredientSorterStages();
}
