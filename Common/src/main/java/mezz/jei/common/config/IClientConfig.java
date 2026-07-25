package mezz.jei.common.config;

import mezz.jei.api.runtime.config.IJeiConfigValue;
import mezz.jei.common.config.file.IConfigListener;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public interface IClientConfig {
	int minRecipeGuiHeight = 175;
	int defaultRecipeGuiHeight = 350;
	boolean defaultCenterSearchBar = false;

	boolean isCenterSearchBarEnabled();

	void addCenterSearchBarEnabledListener(Consumer<Boolean> listener);

	void addMaxRecipeGuiHeightListener(Consumer<Integer> listener);

	boolean isLowMemorySlowSearchEnabled();

	void addLowMemorySlowSearchEnabledListener(Consumer<Boolean> listener);

	boolean isCatchRenderErrorsEnabled();

	boolean isRecipeSyncWarningEnabled();

	boolean isCheatToHotbarUsingHotkeysEnabled();

	GiveMode getGiveMode();

	boolean getShowHiddenIngredients();

	boolean isDragToRearrangeBookmarksEnabled();

	boolean isLookupHistoryEnabled();

	void setLookupHistoryEnabled(boolean enabled);

	void addLookupHistoryEnabledListener(IConfigListener<Boolean> listener);

	int getMaxLookupHistoryRows();

	int getMaxLookupHistoryIngredients();

	HistoryDisplaySide getLookupHistoryDisplaySide();

	void addLookupHistoryDisplaySideListener(IConfigListener<HistoryDisplaySide> listener);
	IJeiConfigValue<Boolean> recipeSlotCyclingEnabled();

	boolean isIngredientsSummaryEnabled();

	int getDragDelayMs();

	int getSmoothScrollRate();

	List<BookmarkTooltipFeature> getBookmarkTooltipFeatures();

	boolean isHoldShiftToShowBookmarkTooltipFeaturesEnabled();

	int getMaxRecipeGuiHeight();

	List<IngredientSortStage> getIngredientSorterStages();

	void addIngredientSorterStagesListener(Consumer<List<IngredientSortStage>> listener);

	Set<RecipeSorterStage> getRecipeSorterStages();

	void enableRecipeSorterStage(RecipeSorterStage stage);

	void disableRecipeSorterStage(RecipeSorterStage stage);

	boolean isTagContentTooltipEnabled();

	boolean getHideSingleTagContentTooltipEnabled();

	boolean isLookupFluidContentsEnabled();

	boolean isAddingBookmarksToFrontEnabled();

	boolean isBookmarkOutputAsRecipeEnabled();

	boolean isShowTagRecipesEnabled();

	boolean isShowCreativeTabNamesEnabled();

	boolean isToastReflowEnabled();
}
