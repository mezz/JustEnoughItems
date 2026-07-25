package mezz.jei.test.lib;

import mezz.jei.api.runtime.config.IJeiConfigValue;
import mezz.jei.common.config.BookmarkTooltipFeature;
import mezz.jei.common.config.GiveMode;
import mezz.jei.common.config.HistoryDisplaySide;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.config.IngredientSortStage;
import mezz.jei.common.config.RecipeSorterStage;

import java.util.List;

public class TestClientConfig implements IClientConfig {
	private final IJeiConfigValue<Boolean> centerSearchBarEnabled = value("centerSearchBarEnabled", false);
	private final IJeiConfigValue<Integer> maxRecipeGuiHeight = value("maxRecipeGuiHeight", 500);
	private final IJeiConfigValue<Boolean> toastReflowEnabled = value("toastReflowEnabled", true);
	private final IJeiConfigValue<GiveMode> giveMode = value("giveMode", GiveMode.INVENTORY);
	private final IJeiConfigValue<Boolean> cheatToHotbarUsingHotkeysEnabled = value("cheatToHotbarUsingHotkeysEnabled", false);
	private final IJeiConfigValue<Boolean> showHiddenIngredients = value("showHiddenIngredients", false);
	private final IJeiConfigValue<Boolean> showTagRecipesEnabled = value("showTagRecipesEnabled", false);
	private final IJeiConfigValue<Boolean> addBookmarksToFrontEnabled = value("addBookmarksToFrontEnabled", false);
	private final IJeiConfigValue<Boolean> bookmarkOutputAsRecipe = value("bookmarkOutputAsRecipe", true);
	private final IJeiConfigValue<List<BookmarkTooltipFeature>> bookmarkTooltipFeatures = value("bookmarkTooltipFeatures", List.of());
	private final IJeiConfigValue<Boolean> holdShiftToShowBookmarkTooltipFeaturesEnabled = value("holdShiftToShowBookmarkTooltipFeaturesEnabled", true);
	private final IJeiConfigValue<Boolean> dragToRearrangeBookmarksEnabled = value("dragToRearrangeBookmarksEnabled", false);
	private final IJeiConfigValue<Boolean> lookupHistoryEnabled = value("lookupHistoryEnabled", false);
	private final IJeiConfigValue<Integer> maxLookupHistoryRows = value("maxLookupHistoryRows", 0);
	private final IJeiConfigValue<Integer> maxLookupHistoryIngredients = value("maxLookupHistoryIngredients", 0);
	private final IJeiConfigValue<HistoryDisplaySide> lookupHistoryDisplaySide = value("lookupHistoryDisplaySide", HistoryDisplaySide.LEFT);
	private final IJeiConfigValue<Boolean> ingredientsSummaryEnabled = value("ingredientsSummaryEnabled", true);
	private final IJeiConfigValue<Boolean> lowMemorySlowSearchEnabled;
	private final IJeiConfigValue<Boolean> catchRenderErrorsEnabled = value("catchRenderErrorsEnabled", false);
	private final IJeiConfigValue<Boolean> lookupFluidContentsEnabled = value("lookupFluidContentsEnabled", false);
	private final IJeiConfigValue<Boolean> lookupBlockTagsEnabled = value("lookupBlockTagsEnabled", false);
	private final IJeiConfigValue<Boolean> showCreativeTabNamesEnabled = value("showCreativeTabNamesEnabled", false);
	private final IJeiConfigValue<Integer> dragDelayMs = value("dragDelayMs", 0);
	private final IJeiConfigValue<Integer> smoothScrollRate = value("smoothScrollRate", 9);
	private final IJeiConfigValue<List<IngredientSortStage>> ingredientSorterStages = value("ingredientSorterStages", List.of());
	private final IJeiConfigValue<List<RecipeSorterStage>> recipeSorterStages = value("recipeSorterStages", List.of());
	private final IJeiConfigValue<Boolean> tagContentTooltipEnabled = value("tagContentTooltipEnabled", true);
	private final IJeiConfigValue<Boolean> hideSingleTagContentTooltipEnabled = value("hideSingleTagContentTooltipEnabled", true);

	public TestClientConfig(boolean lowMemorySlowSearchEnabled) {
		this.lowMemorySlowSearchEnabled = value("lowMemorySlowSearchEnabled", lowMemorySlowSearchEnabled);
	}

	private static <T> IJeiConfigValue<T> value(String name, T value) {
		return new TestJeiConfigValue<>(name, value);
	}

	@Override
	public IJeiConfigValue<Boolean> centerSearchBarEnabled() {
		return centerSearchBarEnabled;
	}

	@Override
	public IJeiConfigValue<Integer> maxRecipeGuiHeight() {
		return maxRecipeGuiHeight;
	}

	@Override
	public IJeiConfigValue<Boolean> toastReflowEnabled() {
		return toastReflowEnabled;
	}

	@Override
	public IJeiConfigValue<GiveMode> giveMode() {
		return giveMode;
	}

	@Override
	public IJeiConfigValue<Boolean> cheatToHotbarUsingHotkeysEnabled() {
		return cheatToHotbarUsingHotkeysEnabled;
	}

	@Override
	public IJeiConfigValue<Boolean> showHiddenIngredients() {
		return showHiddenIngredients;
	}

	@Override
	public IJeiConfigValue<Boolean> showTagRecipesEnabled() {
		return showTagRecipesEnabled;
	}

	@Override
	public IJeiConfigValue<Boolean> addBookmarksToFrontEnabled() {
		return addBookmarksToFrontEnabled;
	}

	@Override
	public IJeiConfigValue<Boolean> bookmarkOutputAsRecipe() {
		return bookmarkOutputAsRecipe;
	}

	@Override
	public IJeiConfigValue<List<BookmarkTooltipFeature>> bookmarkTooltipFeatures() {
		return bookmarkTooltipFeatures;
	}

	@Override
	public IJeiConfigValue<Boolean> holdShiftToShowBookmarkTooltipFeaturesEnabled() {
		return holdShiftToShowBookmarkTooltipFeaturesEnabled;
	}

	@Override
	public IJeiConfigValue<Boolean> dragToRearrangeBookmarksEnabled() {
		return dragToRearrangeBookmarksEnabled;
	}

	@Override
	public IJeiConfigValue<Boolean> lookupHistoryEnabled() {
		return lookupHistoryEnabled;
	}

	@Override
	public IJeiConfigValue<Integer> maxLookupHistoryRows() {
		return maxLookupHistoryRows;
	}

	@Override
	public IJeiConfigValue<Integer> maxLookupHistoryIngredients() {
		return maxLookupHistoryIngredients;
	}

	@Override
	public IJeiConfigValue<HistoryDisplaySide> lookupHistoryDisplaySide() {
		return lookupHistoryDisplaySide;
	}

	@Override
	public IJeiConfigValue<Boolean> ingredientsSummaryEnabled() {
		return ingredientsSummaryEnabled;
	}

	@Override
	public IJeiConfigValue<Boolean> lowMemorySlowSearchEnabled() {
		return lowMemorySlowSearchEnabled;
	}

	@Override
	public IJeiConfigValue<Boolean> catchRenderErrorsEnabled() {
		return catchRenderErrorsEnabled;
	}

	@Override
	public IJeiConfigValue<Boolean> lookupFluidContentsEnabled() {
		return lookupFluidContentsEnabled;
	}

	@Override
	public IJeiConfigValue<Boolean> lookupBlockTagsEnabled() {
		return lookupBlockTagsEnabled;
	}

	@Override
	public IJeiConfigValue<Boolean> showCreativeTabNamesEnabled() {
		return showCreativeTabNamesEnabled;
	}

	@Override
	public IJeiConfigValue<Integer> dragDelayMs() {
		return dragDelayMs;
	}

	@Override
	public IJeiConfigValue<Integer> smoothScrollRate() {
		return smoothScrollRate;
	}

	@Override
	public IJeiConfigValue<List<IngredientSortStage>> ingredientSorterStages() {
		return ingredientSorterStages;
	}

	@Override
	public IJeiConfigValue<List<RecipeSorterStage>> recipeSorterStages() {
		return recipeSorterStages;
	}

	@Override
	public IJeiConfigValue<Boolean> tagContentTooltipEnabled() {
		return tagContentTooltipEnabled;
	}

	@Override
	public IJeiConfigValue<Boolean> hideSingleTagContentTooltipEnabled() {
		return hideSingleTagContentTooltipEnabled;
	}
}
