package mezz.jei.config;

import mezz.jei.ingredients.IngredientSortStage;
import mezz.jei.util.GiveMode;

import java.util.List;

public interface IClientConfig {
	boolean isDebugModeEnabled();

	boolean isCenterSearchBarEnabled();

	boolean isLowMemorySlowSearchEnabled();

	GiveMode getGiveMode();

	int getMaxColumns();

	int getMaxRecipeGuiHeight();

	List<IngredientSortStage> getIngredientSorterStages();
}
