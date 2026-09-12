package mezz.jei.common.config;

import net.mezzdev.config.api.value.IConfigValue;

import java.util.List;

public interface IClientConfig {
	int minRecipeGuiHeight = 175;
	int defaultRecipeGuiHeight = 350;
	int maximumRecipeGuiHeight = 7680;
	boolean defaultCenterSearchBar = false;

	IConfigValue<SearchBarPosition> searchBarPosition();

	IConfigValue<Integer> maxRecipeGuiHeight();

	IConfigValue<Boolean> toastReflowEnabled();

	IConfigValue<GiveMode> giveMode();

	IConfigValue<Boolean> cheatToHotbarUsingHotkeysEnabled();

	IConfigValue<Boolean> showHiddenIngredients();

	IConfigValue<BookmarkAddPosition> bookmarkAddPosition();

	IConfigValue<Boolean> bookmarkOutputAsRecipe();

	IConfigValue<Boolean> bookmarkTooltipPreviewEnabled();

	IConfigValue<Boolean> bookmarkTooltipIngredientsEnabled();

	IConfigValue<Boolean> holdShiftToShowBookmarkTooltipFeaturesEnabled();

	IConfigValue<Boolean> dragToRearrangeBookmarksEnabled();

	IConfigValue<Boolean> lookupHistoryEnabled();

	IConfigValue<Integer> maxLookupHistoryRows();

	IConfigValue<Integer> maxLookupHistoryIngredients();

	IConfigValue<HistoryDisplaySide> lookupHistoryDisplaySide();

	IConfigValue<Boolean> ingredientsSummaryEnabled();

	IConfigValue<Boolean> showTagRecipesEnabled();

	IConfigValue<Boolean> lowMemorySlowSearchEnabled();

	IConfigValue<Boolean> catchRenderErrorsEnabled();

	IConfigValue<Boolean> recipeSyncWarningEnabled();

	IConfigValue<Boolean> lookupFluidContentsEnabled();

	IConfigValue<Boolean> lookupBlockTagsEnabled();

	IConfigValue<Boolean> showCreativeTabNamesEnabled();

	IConfigValue<Integer> dragDelayMs();

	IConfigValue<Integer> smoothScrollRate();

	IConfigValue<Boolean> recipeSlotCyclingEnabled();

	IConfigValue<List<IngredientSortStage>> ingredientSorterStages();

	IConfigValue<Boolean> recipeSortingBookmarksEnabled();

	IConfigValue<Boolean> recipeSortingCraftableEnabled();

	IConfigValue<Boolean> tagContentTooltipEnabled();

	IConfigValue<Boolean> hideSingleTagContentTooltipEnabled();
}
