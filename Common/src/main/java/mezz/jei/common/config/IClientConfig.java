package mezz.jei.common.config;

import mezz.jei.common.config.file.IConfigListener;

import java.util.List;
import java.util.Set;

public interface IClientConfig {
	int minRecipeGuiHeight = 175;
	int defaultRecipeGuiHeight = 350;
	boolean defaultCenterSearchBar = false;

	boolean isCenterSearchBarEnabled();

	void addCenterSearchBarEnabledListener(IConfigListener<Boolean> listener);

	void addMaxRecipeGuiHeightListener(IConfigListener<Integer> listener);

	boolean isLowMemorySlowSearchEnabled();

	void addLowMemorySlowSearchEnabledListener(IConfigListener<Boolean> listener);

	boolean isCatchRenderErrorsEnabled();

	boolean isCheatToHotbarUsingHotkeysEnabled();

	boolean isAddingBookmarksToFrontEnabled();

	boolean isBookmarkOutputAsRecipeEnabled();

	boolean isLookupFluidContentsEnabled();

	boolean isLookupBlockTagsEnabled();

	GiveMode getGiveMode();

	boolean getShowHiddenIngredients();

	List<BookmarkTooltipFeature> getBookmarkTooltipFeatures();

	boolean isHoldShiftToShowBookmarkTooltipFeaturesEnabled();

	boolean isDragToRearrangeBookmarksEnabled();

	boolean isLookupHistoryEnabled();

	void setLookupHistoryEnabled(boolean enabled);

	void addLookupHistoryEnabledListener(IConfigListener<Boolean> listener);

	int getMaxLookupHistoryRows();

	int getMaxLookupHistoryIngredients();

	HistoryDisplaySide getLookupHistoryDisplaySide();

	void addLookupHistoryDisplaySideListener(IConfigListener<HistoryDisplaySide> listener);

	void addMaxLookupHistoryRowsListener(IConfigListener<Integer> listener);

	void addMaxLookupHistoryIngredientsListener(IConfigListener<Integer> listener);

	int getDragDelayMs();

	int getSmoothScrollRate();

	int getMaxRecipeGuiHeight();

	List<IngredientSortStage> getIngredientSorterStages();

	void addIngredientSorterStagesListener(IConfigListener<List<IngredientSortStage>> listener);

	Set<RecipeSorterStage> getRecipeSorterStages();

	void enableRecipeSorterStage(RecipeSorterStage stage);

	void disableRecipeSorterStage(RecipeSorterStage stage);

	boolean isTagContentTooltipEnabled();

	boolean getHideSingleTagContentTooltipEnabled();

	boolean isShowTagRecipesEnabled();

	boolean isShowCreativeTabNamesEnabled();
}
