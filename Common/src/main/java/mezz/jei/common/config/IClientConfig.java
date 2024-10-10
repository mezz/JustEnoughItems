package mezz.jei.common.config;

import java.util.List;
import java.util.Set;

public interface IClientConfig {
	int minRecipeGuiHeight = 175;
	int defaultRecipeGuiHeight = 350;
	boolean defaultCenterSearchBar = false;

	boolean isCenterSearchBarEnabled();

	boolean isLowMemorySlowSearchEnabled();

	boolean isCatchRenderErrorsEnabled();

	boolean isCheatToHotbarUsingHotkeysEnabled();

	boolean isAddingBookmarksToFrontEnabled();

	boolean isLookupFluidContentsEnabled();

	boolean isLookupBlockTagsEnabled();

	GiveMode getGiveMode();

	boolean getShowHiddenIngredients();

	List<BookmarkTooltipFeature> getBookmarkTooltipFeatures();

	boolean isHoldShiftToShowBookmarkTooltipFeaturesEnabled();

	boolean isDragToRearrangeBookmarksEnabled();

	int getDragDelayMs();

	int getSmoothScrollRate();

	int getMaxRecipeGuiHeight();

	List<IngredientSortStage> getIngredientSorterStages();

	Set<RecipeSorterStage> getRecipeSorterStages();

	void enableRecipeSorterStage(RecipeSorterStage stage);

	void disableRecipeSorterStage(RecipeSorterStage stage);

	boolean isTagContentTooltipEnabled();

	boolean getHideSingleTagContentTooltipEnabled();

	boolean isShowTagRecipesEnabled();

	boolean isShowCreativeTabNamesEnabled();
}
