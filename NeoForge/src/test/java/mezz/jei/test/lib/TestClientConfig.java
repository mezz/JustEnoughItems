package mezz.jei.test.lib;

import net.mezzdev.config.api.value.IConfigValue;
import mezz.jei.common.config.BookmarkAddPosition;
import mezz.jei.common.config.GiveMode;
import mezz.jei.common.config.HistoryDisplaySide;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.config.IngredientSortStage;
import mezz.jei.common.config.SearchBarPosition;

import java.util.List;

public class TestClientConfig implements IClientConfig {
	private final IConfigValue<SearchBarPosition> searchBarPosition = value("searchBarPosition", SearchBarPosition.STANDARD);
	private final IConfigValue<Integer> maxRecipeGuiHeight = value("maxRecipeGuiHeight", 500);
	private final IConfigValue<Boolean> toastReflowEnabled = value("toastReflowEnabled", true);
	private final IConfigValue<GiveMode> giveMode = value("giveMode", GiveMode.INVENTORY);
	private final IConfigValue<Boolean> cheatToHotbarUsingHotkeysEnabled = value("cheatToHotbarUsingHotkeysEnabled", false);
	private final IConfigValue<Boolean> showHiddenIngredients = value("showHiddenIngredients", false);
	private final IConfigValue<BookmarkAddPosition> bookmarkAddPosition = value("bookmarkAddPosition", BookmarkAddPosition.END);
	private final IConfigValue<Boolean> bookmarkOutputAsRecipe = value("bookmarkOutputAsRecipe", true);
	private final IConfigValue<Boolean> bookmarkTooltipPreviewEnabled = value("bookmarkTooltipPreviewEnabled", false);
	private final IConfigValue<Boolean> bookmarkTooltipIngredientsEnabled = value("bookmarkTooltipIngredientsEnabled", false);
	private final IConfigValue<Boolean> holdShiftToShowBookmarkTooltipFeaturesEnabled = value("holdShiftToShowBookmarkTooltipFeaturesEnabled", true);
	private final IConfigValue<Boolean> dragToRearrangeBookmarksEnabled = value("dragToRearrangeBookmarksEnabled", false);
	private final IConfigValue<Boolean> lookupHistoryEnabled = value("lookupHistoryEnabled", false);
	private final IConfigValue<Integer> maxLookupHistoryRows = value("maxLookupHistoryRows", 0);
	private final IConfigValue<Integer> maxLookupHistoryIngredients = value("maxLookupHistoryIngredients", 0);
	private final IConfigValue<HistoryDisplaySide> lookupHistoryDisplaySide = value("lookupHistoryDisplaySide", HistoryDisplaySide.LEFT);
	private final IConfigValue<Boolean> ingredientsSummaryEnabled = value("ingredientsSummaryEnabled", true);
	private final IConfigValue<Boolean> showTagRecipesEnabled = value("showTagRecipesEnabled", false);
	private final IConfigValue<Boolean> lowMemorySlowSearchEnabled;
	private final IConfigValue<Boolean> catchRenderErrorsEnabled = value("catchRenderErrorsEnabled", false);
	private final IConfigValue<Boolean> recipeSyncWarningEnabled = value("recipeSyncWarningEnabled", true);
	private final IConfigValue<Boolean> lookupFluidContentsEnabled = value("lookupFluidContentsEnabled", false);
	private final IConfigValue<Boolean> lookupBlockTagsEnabled = value("lookupBlockTagsEnabled", false);
	private final IConfigValue<Boolean> showCreativeTabNamesEnabled = value("showCreativeTabNamesEnabled", false);
	private final IConfigValue<Integer> dragDelayMs = value("dragDelayMs", 0);
	private final IConfigValue<Integer> smoothScrollRate = value("smoothScrollRate", 9);
	private final IConfigValue<Boolean> recipeSlotCyclingEnabled = value("recipeSlotCyclingEnabled", false);
	private final IConfigValue<List<IngredientSortStage>> ingredientSorterStages = value("ingredientSorterStages", List.<IngredientSortStage>of());
	private final IConfigValue<Boolean> recipeSortingBookmarksEnabled = value("recipeSortingBookmarksEnabled", false);
	private final IConfigValue<Boolean> recipeSortingCraftableEnabled = value("recipeSortingCraftableEnabled", false);
	private final IConfigValue<Boolean> tagContentTooltipEnabled = value("tagContentTooltipEnabled", true);
	private final IConfigValue<Boolean> hideSingleTagContentTooltipEnabled = value("hideSingleTagContentTooltipEnabled", true);

	public TestClientConfig(boolean lowMemorySlowSearchEnabled) {
		this.lowMemorySlowSearchEnabled = value("lowMemorySlowSearchEnabled", lowMemorySlowSearchEnabled);
	}

	private static <T> IConfigValue<T> value(String name, T value) {
		return new TestJeiConfigValue<>(name, value);
	}

	@Override
	public IConfigValue<SearchBarPosition> searchBarPosition() {
		return searchBarPosition;
	}

	@Override
	public IConfigValue<Integer> maxRecipeGuiHeight() {
		return maxRecipeGuiHeight;
	}

	@Override
	public IConfigValue<Boolean> toastReflowEnabled() {
		return toastReflowEnabled;
	}

	@Override
	public IConfigValue<GiveMode> giveMode() {
		return giveMode;
	}

	@Override
	public IConfigValue<Boolean> cheatToHotbarUsingHotkeysEnabled() {
		return cheatToHotbarUsingHotkeysEnabled;
	}

	@Override
	public IConfigValue<Boolean> showHiddenIngredients() {
		return showHiddenIngredients;
	}

	@Override
	public IConfigValue<BookmarkAddPosition> bookmarkAddPosition() {
		return bookmarkAddPosition;
	}

	@Override
	public IConfigValue<Boolean> bookmarkOutputAsRecipe() {
		return bookmarkOutputAsRecipe;
	}

	@Override
	public IConfigValue<Boolean> bookmarkTooltipPreviewEnabled() {
		return bookmarkTooltipPreviewEnabled;
	}

	@Override
	public IConfigValue<Boolean> bookmarkTooltipIngredientsEnabled() {
		return bookmarkTooltipIngredientsEnabled;
	}

	@Override
	public IConfigValue<Boolean> holdShiftToShowBookmarkTooltipFeaturesEnabled() {
		return holdShiftToShowBookmarkTooltipFeaturesEnabled;
	}

	@Override
	public IConfigValue<Boolean> dragToRearrangeBookmarksEnabled() {
		return dragToRearrangeBookmarksEnabled;
	}

	@Override
	public IConfigValue<Boolean> lookupHistoryEnabled() {
		return lookupHistoryEnabled;
	}

	@Override
	public IConfigValue<Integer> maxLookupHistoryRows() {
		return maxLookupHistoryRows;
	}

	@Override
	public IConfigValue<Integer> maxLookupHistoryIngredients() {
		return maxLookupHistoryIngredients;
	}

	@Override
	public IConfigValue<HistoryDisplaySide> lookupHistoryDisplaySide() {
		return lookupHistoryDisplaySide;
	}

	@Override
	public IConfigValue<Boolean> ingredientsSummaryEnabled() {
		return ingredientsSummaryEnabled;
	}

	@Override
	public IConfigValue<Boolean> showTagRecipesEnabled() {
		return showTagRecipesEnabled;
	}

	@Override
	public IConfigValue<Boolean> lowMemorySlowSearchEnabled() {
		return lowMemorySlowSearchEnabled;
	}

	@Override
	public IConfigValue<Boolean> catchRenderErrorsEnabled() {
		return catchRenderErrorsEnabled;
	}

	@Override
	public IConfigValue<Boolean> recipeSyncWarningEnabled() {
		return recipeSyncWarningEnabled;
	}

	@Override
	public IConfigValue<Boolean> lookupFluidContentsEnabled() {
		return lookupFluidContentsEnabled;
	}

	@Override
	public IConfigValue<Boolean> lookupBlockTagsEnabled() {
		return lookupBlockTagsEnabled;
	}

	@Override
	public IConfigValue<Boolean> showCreativeTabNamesEnabled() {
		return showCreativeTabNamesEnabled;
	}

	@Override
	public IConfigValue<Integer> dragDelayMs() {
		return dragDelayMs;
	}

	@Override
	public IConfigValue<Integer> smoothScrollRate() {
		return smoothScrollRate;
	}

	@Override
	public IConfigValue<Boolean> recipeSlotCyclingEnabled() {
		return recipeSlotCyclingEnabled;
	}

	@Override
	public IConfigValue<List<IngredientSortStage>> ingredientSorterStages() {
		return ingredientSorterStages;
	}

	@Override
	public IConfigValue<Boolean> recipeSortingBookmarksEnabled() {
		return recipeSortingBookmarksEnabled;
	}

	@Override
	public IConfigValue<Boolean> recipeSortingCraftableEnabled() {
		return recipeSortingCraftableEnabled;
	}

	@Override
	public IConfigValue<Boolean> tagContentTooltipEnabled() {
		return tagContentTooltipEnabled;
	}

	@Override
	public IConfigValue<Boolean> hideSingleTagContentTooltipEnabled() {
		return hideSingleTagContentTooltipEnabled;
	}
}
