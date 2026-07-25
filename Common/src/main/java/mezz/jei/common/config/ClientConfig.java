package mezz.jei.common.config;

import com.google.common.base.Preconditions;
import mezz.jei.common.config.file.ConfigValue;
import mezz.jei.common.config.file.IConfigCategoryBuilder;
import mezz.jei.common.config.file.IConfigSchemaBuilder;
import mezz.jei.common.config.file.serializers.EnumSerializer;
import mezz.jei.common.config.file.serializers.ListSerializer;
import mezz.jei.common.platform.Services;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class ClientConfig implements IClientConfig {
	@Nullable
	private static IClientConfig instance;

	// appearance
	private final ConfigValue<Boolean> centerSearchBarEnabled;
	private final ConfigValue<Integer> maxRecipeGuiHeight;
	private final ConfigValue<Boolean> toastReflowEnabled;

	// cheat_mode
	private final ConfigValue<GiveMode> giveMode;
	private final ConfigValue<Boolean> cheatToHotbarUsingHotkeysEnabled;
	private final ConfigValue<Boolean> showHiddenIngredients;

	// bookmarks
	private final ConfigValue<Boolean> addBookmarksToFrontEnabled;
	private final ConfigValue<Boolean> bookmarkOutputAsRecipe;
	private final ConfigValue<List<BookmarkTooltipFeature>> bookmarkTooltipFeatures;
	private final ConfigValue<Boolean> holdShiftToShowBookmarkTooltipFeaturesEnabled;
	private final ConfigValue<Boolean> dragToRearrangeBookmarksEnabled;

	// lookup history
	private final ConfigValue<Boolean> lookupHistoryEnabled;
	private final ConfigValue<Integer> maxLookupHistoryRows;
	private final ConfigValue<Integer> maxLookupHistoryIngredients;
	private final ConfigValue<HistoryDisplaySide> lookupHistoryDisplaySide;

	// recipes gui
	private final ConfigValue<Boolean> ingredientsSummaryEnabled;

	// advanced
	private final ConfigValue<Boolean> lowMemorySlowSearchEnabled;
	private final ConfigValue<Boolean> catchRenderErrorsEnabled;
	private final ConfigValue<Boolean> lookupFluidContentsEnabled;
	private final ConfigValue<Boolean> lookupBlockTagsEnabled;
	private final ConfigValue<Boolean> showTagRecipesEnabled;
	private final ConfigValue<Boolean> showCreativeTabNamesEnabled;

	// input
	private final ConfigValue<Integer> dragDelayMs;
	private final ConfigValue<Integer> smoothScrollRate;

	// sorting
	private final ConfigValue<List<IngredientSortStage>> ingredientSorterStages;
	private final ConfigValue<List<RecipeSorterStage>> recipeSorterStages;

	// tags
	private final ConfigValue<Boolean> tagContentTooltipEnabled;
	private final ConfigValue<Boolean> hideSingleTagContentTooltipEnabled;

	public ClientConfig(IConfigSchemaBuilder schema) {
		instance = this;

		boolean isDev = Services.PLATFORM.getModHelper().isInDev();

		IConfigCategoryBuilder appearance = schema.addCategory("appearance");
		centerSearchBarEnabled = appearance.addBoolean("centerSearch", defaultCenterSearchBar);
		maxRecipeGuiHeight = appearance.addInteger(
			"recipeGuiHeight",
			defaultRecipeGuiHeight,
			minRecipeGuiHeight,
			Integer.MAX_VALUE
		);
		toastReflowEnabled = appearance.addBoolean("toastReflowEnabled", true);

		IConfigCategoryBuilder cheating = schema.addCategory("cheating");
		giveMode = cheating.addEnum("giveMode", GiveMode.defaultGiveMode);
		cheatToHotbarUsingHotkeysEnabled = cheating.addBoolean("cheatToHotbarUsingHotkeysEnabled", false);
		showHiddenIngredients = cheating.addBoolean("showHiddenIngredients", false);
		showTagRecipesEnabled = cheating.addBoolean("showTagRecipesEnabled", isDev);

		IConfigCategoryBuilder bookmarks = schema.addCategory("bookmarks");
		addBookmarksToFrontEnabled = bookmarks.addBoolean("addBookmarksToFrontEnabled", false);
		bookmarkOutputAsRecipe = bookmarks.addBoolean("bookmarkOutputAsRecipe", true);
		dragToRearrangeBookmarksEnabled = bookmarks.addBoolean("dragToRearrangeBookmarksEnabled", true);

		IConfigCategoryBuilder tooltips = schema.addCategory("tooltips");
		bookmarkTooltipFeatures = tooltips.addList(
			"bookmarkTooltipFeatures",
			BookmarkTooltipFeature.DEFAULT_BOOKMARK_TOOLTIP_FEATURES,
			new ListSerializer<>(new EnumSerializer<>(BookmarkTooltipFeature.class))
		);
		holdShiftToShowBookmarkTooltipFeaturesEnabled = tooltips.addBoolean("holdShiftToShowBookmarkTooltipFeatures", true);
		showCreativeTabNamesEnabled = tooltips.addBoolean("showCreativeTabNamesEnabled", false);
		tagContentTooltipEnabled = tooltips.addBoolean("tagContentTooltipEnabled", true);
		hideSingleTagContentTooltipEnabled = tooltips.addBoolean("hideSingleTagContentTooltipEnabled", true);
		ingredientsSummaryEnabled = tooltips.addBoolean("enableRecipesGuiIngredientsSummary", false);

		IConfigCategoryBuilder performance = schema.addCategory("performance");
		lowMemorySlowSearchEnabled = performance.addBoolean("lowMemorySlowSearchEnabled", false);

		IConfigCategoryBuilder lookups = schema.addCategory("lookups");
		lookupFluidContentsEnabled = lookups.addBoolean("lookupFluidContentsEnabled", false);
		lookupBlockTagsEnabled = lookups.addBoolean("lookupBlockTagsEnabled", true);

		IConfigCategoryBuilder lookupHistory = schema.addCategory("lookupHistory");

		lookupHistoryEnabled = lookupHistory.addBoolean(
			"enabled",
			false
		);
		maxLookupHistoryRows = lookupHistory.addInteger(
			"maxRows",
			2,
			1,
			7
		);
		maxLookupHistoryIngredients = lookupHistory.addInteger(
			"maxIngredients",
			100,
			10,
			1_000
		);
		lookupHistoryDisplaySide = lookupHistory.addEnum(
			"displaySide",
			HistoryDisplaySide.LEFT
		);

		IConfigCategoryBuilder advanced = schema.addCategory("advanced");
		catchRenderErrorsEnabled = advanced.addBoolean("catchRenderErrorsEnabled", !isDev);

		IConfigCategoryBuilder input = schema.addCategory("input");
		dragDelayMs = input.addInteger(
			"dragDelayInMilliseconds",
			150,
			0,
			1000
		);
		smoothScrollRate = input.addInteger(
			"smoothScrollRate",
			9,
			1,
			50
		);

		IConfigCategoryBuilder sorting = schema.addCategory("sorting");
		ingredientSorterStages = sorting.addList(
			"ingredientSortStages",
			IngredientSortStage.defaultStages,
			new ListSerializer<>(new EnumSerializer<>(IngredientSortStage.class))
		);
		recipeSorterStages = sorting.addList(
			"recipeSorterStages",
			RecipeSorterStage.defaultStages,
			new ListSerializer<>(new EnumSerializer<>(RecipeSorterStage.class))
		);
	}

	/**
	 * Only use this for hacky stuff like the debug plugin
	 */
	@Deprecated
	public static IClientConfig getInstance() {
		Preconditions.checkNotNull(instance);
		return instance;
	}

	@Override
	public ConfigValue<Boolean> centerSearchBarEnabled() {
		return centerSearchBarEnabled;
	}

	@Override
	public ConfigValue<Integer> maxRecipeGuiHeight() {
		return maxRecipeGuiHeight;
	}

	@Override
	public ConfigValue<Boolean> toastReflowEnabled() {
		return toastReflowEnabled;
	}

	@Override
	public ConfigValue<GiveMode> giveMode() {
		return giveMode;
	}

	@Override
	public ConfigValue<Boolean> cheatToHotbarUsingHotkeysEnabled() {
		return cheatToHotbarUsingHotkeysEnabled;
	}

	@Override
	public ConfigValue<Boolean> showHiddenIngredients() {
		return showHiddenIngredients;
	}

	@Override
	public ConfigValue<Boolean> showTagRecipesEnabled() {
		return showTagRecipesEnabled;
	}

	@Override
	public ConfigValue<Boolean> addBookmarksToFrontEnabled() {
		return addBookmarksToFrontEnabled;
	}

	@Override
	public ConfigValue<Boolean> bookmarkOutputAsRecipe() {
		return bookmarkOutputAsRecipe;
	}

	@Override
	public ConfigValue<List<BookmarkTooltipFeature>> bookmarkTooltipFeatures() {
		return bookmarkTooltipFeatures;
	}

	@Override
	public ConfigValue<Boolean> holdShiftToShowBookmarkTooltipFeaturesEnabled() {
		return holdShiftToShowBookmarkTooltipFeaturesEnabled;
	}

	@Override
	public ConfigValue<Boolean> dragToRearrangeBookmarksEnabled() {
		return dragToRearrangeBookmarksEnabled;
	}

	@Override
	public ConfigValue<Boolean> lookupHistoryEnabled() {
		return lookupHistoryEnabled;
	}

	@Override
	public ConfigValue<Integer> maxLookupHistoryRows() {
		return maxLookupHistoryRows;
	}

	@Override
	public ConfigValue<Integer> maxLookupHistoryIngredients() {
		return maxLookupHistoryIngredients;
	}

	@Override
	public ConfigValue<HistoryDisplaySide> lookupHistoryDisplaySide() {
		return lookupHistoryDisplaySide;
	}

	@Override
	public ConfigValue<Boolean> ingredientsSummaryEnabled() {
		return ingredientsSummaryEnabled;
	}

	@Override
	public ConfigValue<Boolean> lowMemorySlowSearchEnabled() {
		return lowMemorySlowSearchEnabled;
	}

	@Override
	public ConfigValue<Boolean> catchRenderErrorsEnabled() {
		return catchRenderErrorsEnabled;
	}

	@Override
	public ConfigValue<Boolean> lookupFluidContentsEnabled() {
		return lookupFluidContentsEnabled;
	}

	@Override
	public ConfigValue<Boolean> lookupBlockTagsEnabled() {
		return lookupBlockTagsEnabled;
	}

	@Override
	public ConfigValue<Boolean> showCreativeTabNamesEnabled() {
		return showCreativeTabNamesEnabled;
	}

	@Override
	public ConfigValue<Integer> dragDelayMs() {
		return dragDelayMs;
	}

	@Override
	public ConfigValue<Integer> smoothScrollRate() {
		return smoothScrollRate;
	}

	@Override
	public ConfigValue<List<IngredientSortStage>> ingredientSorterStages() {
		return ingredientSorterStages;
	}

	@Override
	public ConfigValue<List<RecipeSorterStage>> recipeSorterStages() {
		return recipeSorterStages;
	}

	@Override
	public ConfigValue<Boolean> tagContentTooltipEnabled() {
		return tagContentTooltipEnabled;
	}

	@Override
	public ConfigValue<Boolean> hideSingleTagContentTooltipEnabled() {
		return hideSingleTagContentTooltipEnabled;
	}
}
