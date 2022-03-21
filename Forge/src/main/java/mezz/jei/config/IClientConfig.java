package mezz.jei.config;

import mezz.jei.ingredients.IngredientSortStage;
import mezz.jei.util.GiveMode;

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
