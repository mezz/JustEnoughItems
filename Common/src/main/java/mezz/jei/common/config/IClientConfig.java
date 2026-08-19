package mezz.jei.common.config;

import mezz.jei.api.runtime.config.IJeiConfigValue;

import java.util.List;

public interface IClientConfig {
	int minRecipeGuiHeight = 175;
	int defaultRecipeGuiHeight = 350;
	boolean defaultCenterSearchBar = false;

	IJeiConfigValue<Boolean> centerSearchBarEnabled();

	IJeiConfigValue<Integer> maxRecipeGuiHeight();

	IJeiConfigValue<Boolean> toastReflowEnabled();

	IJeiConfigValue<GiveMode> giveMode();

	IJeiConfigValue<Boolean> cheatToHotbarUsingHotkeysEnabled();

	IJeiConfigValue<Boolean> showHiddenIngredients();

	IJeiConfigValue<Boolean> bookmarkOutputAsRecipeEnabled();

	IJeiConfigValue<Boolean> showTagRecipesEnabled();

	IJeiConfigValue<Boolean> addBookmarksToFrontEnabled();

	IJeiConfigValue<List<BookmarkTooltipFeature>> bookmarkTooltipFeatures();

	IJeiConfigValue<Boolean> holdShiftToShowBookmarkTooltipFeaturesEnabled();

	IJeiConfigValue<Boolean> dragToRearrangeBookmarksEnabled();

	IJeiConfigValue<Boolean> lookupHistoryEnabled();

	IJeiConfigValue<Integer> maxLookupHistoryRows();

	IJeiConfigValue<Integer> maxLookupHistoryIngredients();

	IJeiConfigValue<HistoryDisplaySide> lookupHistoryDisplaySide();

	IJeiConfigValue<Boolean> ingredientsSummaryEnabled();

	IJeiConfigValue<Boolean> lowMemorySlowSearchEnabled();

	IJeiConfigValue<Boolean> catchRenderErrorsEnabled();

	IJeiConfigValue<Boolean> lookupFluidContentsEnabled();

	IJeiConfigValue<Boolean> lookupBlockTagsEnabled();

	IJeiConfigValue<Boolean> showCreativeTabNamesEnabled();

	IJeiConfigValue<Integer> dragDelayMs();

	IJeiConfigValue<Integer> smoothScrollRate();

	IJeiConfigValue<List<IngredientSortStage>> ingredientSorterStages();

	IJeiConfigValue<List<RecipeSorterStage>> recipeSorterStages();

	IJeiConfigValue<Boolean> tagContentTooltipEnabled();

	IJeiConfigValue<Boolean> hideSingleTagContentTooltipEnabled();

	IJeiConfigValue<Boolean> recipeSlotCyclingEnabled();
}
