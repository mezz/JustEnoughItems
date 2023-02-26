package mezz.jei.common.config;

import java.util.List;

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

	List<IngredientSortStage> getIngredientSorterStages();
}
