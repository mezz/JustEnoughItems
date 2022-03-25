package mezz.jei.core.config;

import java.util.List;

public interface IClientConfig {
	boolean isDebugModeEnabled();

	boolean isCenterSearchBarEnabled();

	boolean isLowMemorySlowSearchEnabled();

	boolean isCheatToHotbarUsingHotkeysEnabled();

	GiveMode getGiveMode();

	int getMaxRecipeGuiHeight();

	List<IngredientSortStage> getIngredientSorterStages();
}
