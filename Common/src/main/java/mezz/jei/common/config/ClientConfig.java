package mezz.jei.common.config;

import com.google.common.base.Preconditions;
import mezz.jei.common.config.file.ConfigValue;
import mezz.jei.common.config.file.IConfigCategoryBuilder;
import mezz.jei.common.config.file.IConfigSchemaBuilder;
import mezz.jei.common.config.file.serializers.EnumSerializer;
import mezz.jei.common.config.file.serializers.ListSerializer;
import mezz.jei.common.platform.Services;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public final class ClientConfig implements IClientConfig {
	@Nullable
	private static IClientConfig instance;

	// appearance
	private final Supplier<Boolean> centerSearchBarEnabled;
	private final Supplier<Integer> maxRecipeGuiHeight;

	// cheat_mode
	private final Supplier<GiveMode> giveMode;
	private final Supplier<Boolean> cheatToHotbarUsingHotkeysEnabled;
	private final Supplier<Boolean> showHiddenIngredients;

	// bookmarks
	private final Supplier<Boolean> addBookmarksToFrontEnabled;
	private final Supplier<List<BookmarkTooltipFeature>> bookmarkTooltipFeatures;
	private final Supplier<Boolean> holdShiftToShowBookmarkTooltipFeaturesEnabled;
	private final Supplier<Boolean> dragToRearrangeBookmarksEnabled;

	// history
	private final Supplier<Boolean> historyEnabled;
	private final Supplier<Integer> maxHistoryRows;

	// advanced
	private final Supplier<Boolean> lowMemorySlowSearchEnabled;
	private final Supplier<Boolean> catchRenderErrorsEnabled;
	private final Supplier<Boolean> lookupFluidContentsEnabled;
	private final Supplier<Boolean> lookupBlockTagsEnabled;
	private final Supplier<Boolean> showTagRecipesEnabled;
	private final Supplier<Boolean> showCreativeTabNamesEnabled;

	// input
	private final Supplier<Integer> dragDelayMs;
	private final Supplier<Integer> smoothScrollRate;

	// sorting
	private final Supplier<List<IngredientSortStage>> ingredientSorterStages;
	private final ConfigValue<List<RecipeSorterStage>> recipeSorterStages;

	// tags
	private final Supplier<Boolean> tagContentTooltipEnabled;
	private final Supplier<Boolean> hideSingleTagContentTooltipEnabled;

	public ClientConfig(IConfigSchemaBuilder schema) {
		instance = this;

		boolean isDev = Services.PLATFORM.getModHelper().isInDev();

		IConfigCategoryBuilder appearance = schema.addCategory("appearance");
		centerSearchBarEnabled = appearance.addBoolean("centerSearch",			defaultCenterSearchBar		);
		maxRecipeGuiHeight = appearance.addInteger(
			"recipeGuiHeight",
			defaultRecipeGuiHeight,
			minRecipeGuiHeight,
			Integer.MAX_VALUE
		);

		IConfigCategoryBuilder cheating = schema.addCategory("cheating");
		giveMode = cheating.addEnum("giveMode", GiveMode.defaultGiveMode);
		cheatToHotbarUsingHotkeysEnabled = cheating.addBoolean("cheatToHotbarUsingHotkeysEnabled", false);
		showHiddenIngredients = cheating.addBoolean("showHiddenIngredients", false);
		showTagRecipesEnabled = cheating.addBoolean("showTagRecipesEnabled", isDev);

		IConfigCategoryBuilder bookmarks = schema.addCategory("bookmarks");
		addBookmarksToFrontEnabled = bookmarks.addBoolean("addBookmarksToFrontEnabled", false);
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

		IConfigCategoryBuilder performance = schema.addCategory("performance");
		lowMemorySlowSearchEnabled = performance.addBoolean("lowMemorySlowSearchEnabled", false);

		IConfigCategoryBuilder lookups = schema.addCategory("lookups");
		lookupFluidContentsEnabled = lookups.addBoolean("lookupFluidContentsEnabled", false);
		lookupBlockTagsEnabled = lookups.addBoolean("lookupBlockTagsEnabled", true);

		historyEnabled = bookmarks.addBoolean(
			"HistoryEnabled",
			false,
			"Enable the history overlay."
		);
		maxHistoryRows = bookmarks.addInteger(
			"MaxHistoryRows",
			1,
			0,
			5,
			"Max number of rows in the history overlay."
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
	public boolean isCenterSearchBarEnabled() {
		return centerSearchBarEnabled.get();
	}

	@Override
	public boolean isLowMemorySlowSearchEnabled() {
		return lowMemorySlowSearchEnabled.get();
	}

	@Override
	public boolean isCatchRenderErrorsEnabled() {
		return catchRenderErrorsEnabled.get();
	}

	@Override
	public boolean isCheatToHotbarUsingHotkeysEnabled() {
		return cheatToHotbarUsingHotkeysEnabled.get();
	}

	@Override
	public boolean isAddingBookmarksToFrontEnabled() {
		return addBookmarksToFrontEnabled.get();
	}

	@Override
	public boolean isLookupFluidContentsEnabled() {
		return lookupFluidContentsEnabled.get();
	}

	@Override
	public boolean isLookupBlockTagsEnabled() {
		return lookupBlockTagsEnabled.get();
	}

	@Override
	public GiveMode getGiveMode() {
		return giveMode.get();
	}

	@Override
	public boolean getShowHiddenIngredients() {
		return showHiddenIngredients.get();
	}

	@Override
	public List<BookmarkTooltipFeature> getBookmarkTooltipFeatures() {
		return bookmarkTooltipFeatures.get();
	}

	@Override
	public boolean isHoldShiftToShowBookmarkTooltipFeaturesEnabled() {
		return holdShiftToShowBookmarkTooltipFeaturesEnabled.get();
	}

	@Override
	public boolean isDragToRearrangeBookmarksEnabled() {
		return dragToRearrangeBookmarksEnabled.get();
	}

	@Override
	public boolean isHistoryEnabled() {
		return historyEnabled.get();
	}

	@Override
	public int getMaxHistoryRows() {
		return maxHistoryRows.get();
	}

	@Override
	public int getDragDelayMs() {
		return dragDelayMs.get();
	}

	@Override
	public int getSmoothScrollRate() {
		return smoothScrollRate.get();
	}

	@Override
	public int getMaxRecipeGuiHeight() {
		return maxRecipeGuiHeight.get();
	}

	@Override
	public List<IngredientSortStage> getIngredientSorterStages() {
		return ingredientSorterStages.get();
	}

	@Override
	public Set<RecipeSorterStage> getRecipeSorterStages() {
		return Set.copyOf(recipeSorterStages.getValue());
	}

	@Override
	public void enableRecipeSorterStage(RecipeSorterStage stage) {
		List<RecipeSorterStage> recipeSorterStages = this.recipeSorterStages.get();
		if (!recipeSorterStages.contains(stage)) {
			recipeSorterStages = new ArrayList<>(recipeSorterStages);
			recipeSorterStages.add(stage);
			this.recipeSorterStages.set(recipeSorterStages);
		}
	}

	@Override
	public void disableRecipeSorterStage(RecipeSorterStage stage) {
		List<RecipeSorterStage> recipeSorterStages = this.recipeSorterStages.get();
		if (recipeSorterStages.contains(stage)) {
			recipeSorterStages = new ArrayList<>(recipeSorterStages);
			recipeSorterStages.remove(stage);
			this.recipeSorterStages.set(recipeSorterStages);
		}
	}

	@Override
	public boolean isTagContentTooltipEnabled() {
		return tagContentTooltipEnabled.get();
	}

	@Override
	public boolean getHideSingleTagContentTooltipEnabled() {
		return hideSingleTagContentTooltipEnabled.get();
	}

	@Override
	public boolean isShowTagRecipesEnabled() {
		return showTagRecipesEnabled.get();
	}

	@Override
	public boolean isShowCreativeTabNamesEnabled() {
		return showCreativeTabNamesEnabled.get();
	}
}
