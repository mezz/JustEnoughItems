package mezz.jei.common.config;

import java.util.List;

public interface IClientConfig {
	int minRecipeGuiHeight = 175;
	int defaultRecipeGuiHeight = 350;
	boolean defaultCenterSearchBar = false;

	boolean isCenterSearchBarEnabled();

	boolean isLowMemorySlowSearchEnabled();

	boolean isCatchRenderErrorsEnabled();

	boolean isCatchTooltipRenderErrorsEnabled();

	boolean isCheatToHotbarUsingHotkeysEnabled();

	boolean isAddingBookmarksToFrontEnabled();

	boolean isLookupFluidContentsEnabled();

	boolean isLookupBlockTagsEnabled();

	GiveMode getGiveMode();

	int getMaxRecipeGuiHeight();

	List<IngredientSortStage> getIngredientSorterStages();

	void setIngredientSorterStages(List<IngredientSortStage> ingredientSortStages);

	String getSerializedIngredientSorterStages();

	void setIngredientSorterStages(String ingredientSortStages);
}
