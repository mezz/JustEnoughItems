package mezz.jei.api.config;

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
