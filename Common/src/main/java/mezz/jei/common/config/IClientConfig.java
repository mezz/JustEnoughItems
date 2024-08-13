package mezz.jei.common.config;

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

	boolean isCheatToHotbarUsingHotkeysEnabled();

	GiveMode getGiveMode();

	boolean isShowHiddenItemsEnabled();

	boolean isDragToRearrangeBookmarksEnabled();

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

	boolean isHideSingleIngredientTagsEnabled();

	boolean isLookupFluidContentsEnabled();

	boolean isAddingBookmarksToFrontEnabled();

	boolean isShowTagRecipesEnabled();
}
