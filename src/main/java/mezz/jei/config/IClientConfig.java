package mezz.jei.config;

import mezz.jei.ingredients.IngredientSortStage;
import mezz.jei.util.GiveMode;

import java.util.List;

public interface IClientConfig {
	boolean isDebugModeEnabled();

	boolean isCenterSearchBarEnabled();

	boolean isLowMemorySlowSearchEnabled();

	boolean isFastItemRenderingEnabled();

	boolean isCheatToHotbarUsingHotkeysEnabled();

	GiveMode getGiveMode();

	int getMinColumns();

	int getMaxColumns();

	int getMaxRecipeGuiHeight();

	List<IngredientSortStage> getIngredientSorterStages();
}
