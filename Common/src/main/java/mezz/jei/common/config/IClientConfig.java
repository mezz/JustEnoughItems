package mezz.jei.common.config;

import java.util.List;

public interface IClientConfig {
	int minRecipeGuiHeight = 175;
	int defaultRecipeGuiHeight = 350;
	boolean defaultCenterSearchBar = false;

	boolean isCenterSearchBarEnabled();

	boolean isLowMemorySlowSearchEnabled();

	boolean isCatchRenderErrorsEnabled();

	boolean isCheatToHotbarUsingHotkeysEnabled();

	GiveMode getGiveMode();

	boolean isShowHiddenItemsEnabled();

	boolean isHoldShiftToShowBookmarkTooltipFeaturesEnabled();

	boolean isDragToRearrangeBookmarksEnabled();

	int getDragDelayMs();

	int getSmoothScrollRate();

	int getMaxRecipeGuiHeight();

	List<IngredientSortStage> getIngredientSorterStages();

	boolean isTagContentTooltipEnabled();

	boolean isHideSingleIngredientTagsEnabled();

	boolean isLookupFluidContentsEnabled();

	boolean isAddingBookmarksToFrontEnabled();

	boolean isShowTagRecipesEnabled();

	boolean isShowCreativeTabNamesEnabled();
}
