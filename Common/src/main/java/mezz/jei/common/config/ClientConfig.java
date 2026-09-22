package mezz.jei.common.config;

import com.google.common.base.Preconditions;
import mezz.jei.common.config.legacy.LegacyEnumSerializers;
import net.mezzdev.config.api.schema.builder.IConfigCategoryBuilder;
import net.mezzdev.config.api.schema.builder.IConfigEditorCategoryBuilder;
import net.mezzdev.config.api.value.editor.ConfigValueEditMode;
import net.mezzdev.config.api.value.serializer.IConfigListValueSerializer;
import net.mezzdev.config.api.value.IConfigValue;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class ClientConfig implements IClientConfig {
	@Nullable
	private static IClientConfig instance;

	// appearance
	private final IConfigValue<SearchBarPosition> searchBarPosition;
	private final IConfigValue<Integer> maxRecipeGuiHeight;
	private final IConfigValue<Boolean> toastReflowEnabled;

	// cheat_mode
	private final IConfigValue<GiveMode> giveMode;
	private final IConfigValue<Boolean> cheatToHotbarUsingHotkeysEnabled;
	private final IConfigValue<Boolean> showHiddenIngredients;

	// bookmarks
	private final IConfigValue<BookmarkAddPosition> bookmarkAddPosition;
	private final IConfigValue<Boolean> bookmarkOutputAsRecipe;
	private final IConfigValue<Boolean> bookmarkTooltipPreviewEnabled;
	private final IConfigValue<Boolean> bookmarkTooltipIngredientsEnabled;
	private final IConfigValue<Boolean> holdShiftToShowBookmarkTooltipFeaturesEnabled;
	private final IConfigValue<Boolean> dragToRearrangeBookmarksEnabled;

	// lookup history
	private final IConfigValue<Boolean> lookupHistoryEnabled;
	private final IConfigValue<Integer> maxLookupHistoryRows;
	private final IConfigValue<Integer> maxLookupHistoryIngredients;
	private final IConfigValue<HistoryDisplaySide> lookupHistoryDisplaySide;

	// recipes gui
	private final IConfigValue<Boolean> ingredientsSummaryEnabled;
	private final IConfigValue<Boolean> showTagRecipesEnabled;

	// advanced
	private final IConfigValue<Boolean> lowMemorySlowSearchEnabled;
	private final IConfigValue<Boolean> catchRenderErrorsEnabled;
	private final IConfigValue<Boolean> recipeSyncWarningEnabled;
	private final IConfigValue<Boolean> lookupFluidContentsEnabled;
	private final IConfigValue<Boolean> lookupBlockTagsEnabled;
	private final IConfigValue<Boolean> showCreativeTabNamesEnabled;

	// input
	private final IConfigValue<Integer> dragDelayMs;
	private final IConfigValue<Integer> smoothScrollRate;
	private final IConfigValue<Boolean> recipeSlotCyclingEnabled;

	// sorting
	private final IConfigValue<List<IngredientSortStage>> ingredientSorterStages;
	private final IConfigValue<Boolean> recipeSortingBookmarksEnabled;
	private final IConfigValue<Boolean> recipeSortingCraftableEnabled;

	// tags
	private final IConfigValue<Boolean> tagContentTooltipEnabled;
	private final IConfigValue<Boolean> hideSingleTagContentTooltipEnabled;

	public ClientConfig(
		IConfigCategoryBuilder search,
		IConfigCategoryBuilder ingredientList,
		IConfigEditorCategoryBuilder ingredientSorting,
		IConfigCategoryBuilder bookmarkList,
		IConfigCategoryBuilder input,
		IConfigCategoryBuilder recipes,
		IConfigCategoryBuilder tooltips,
		IConfigCategoryBuilder lookups,
		IConfigCategoryBuilder cheating,
		IConfigCategoryBuilder advanced,
		boolean isDev
	) {
		instance = this;

		searchBarPosition = search.addValue(
				"centerSearch",
				SearchBarPosition.fromCentered(defaultCenterSearchBar),
				LegacyEnumSerializers.enumOrBoolean(SearchBarPosition.class, SearchBarPosition::fromCentered)
			)
			.addLegacyValue("appearance", "centerSearch")
			.setEditMode(ConfigValueEditMode.IMMEDIATE)
			.build();

		IConfigListValueSerializer<IngredientSortStage> ingredientSorterStagesSerializer = LegacyEnumSerializers.list(IngredientSortStage.class);
		ingredientSorterStages = ingredientList.addEnumList("ingredientSortStages", IngredientSortStage.defaultStages, IngredientSortStage.class)
			.addLegacyValueMigration("sorting", "ingredientSortStages", ingredientSorterStagesSerializer, List::copyOf)
			.addEditorCategory(ingredientSorting)
			.setEditMode(ConfigValueEditMode.BATCH)
			.build();
		toastReflowEnabled = ingredientList.addBoolean("toastReflowEnabled", true)
			.addLegacyValue("appearance", "toastReflowEnabled")
			.setEditMode(ConfigValueEditMode.IMMEDIATE)
			.build();

		bookmarkAddPosition = bookmarkList.addValue(
				"addBookmarksToFrontEnabled",
				BookmarkAddPosition.END,
				LegacyEnumSerializers.enumOrBoolean(BookmarkAddPosition.class, ClientConfig::bookmarkAddPositionFromLegacyBoolean)
			)
			.addLegacyValue("bookmarks", "addBookmarksToFrontEnabled")
			.setEditMode(ConfigValueEditMode.IMMEDIATE)
			.build();
		bookmarkOutputAsRecipe = bookmarkList.addBoolean("bookmarkOutputAsRecipe", true)
			.addLegacyValue("bookmarks", "bookmarkOutputAsRecipe")
			.setEditMode(ConfigValueEditMode.IMMEDIATE)
			.build();
		dragToRearrangeBookmarksEnabled = bookmarkList.addBoolean("dragToRearrangeBookmarksEnabled", true)
			.addLegacyValue("bookmarks", "dragToRearrangeBookmarksEnabled")
			.setEditMode(ConfigValueEditMode.IMMEDIATE)
			.build();
		IConfigListValueSerializer<BookmarkTooltipFeature> bookmarkTooltipFeaturesSerializer = LegacyEnumSerializers.list(BookmarkTooltipFeature.class);
		bookmarkTooltipPreviewEnabled = bookmarkList.addBoolean("bookmarkTooltipPreview", true)
			.addLegacyValue("tooltips", "bookmarkTooltipPreview")
			.addLegacyValueMigration(
				"tooltips",
				"bookmarkTooltipFeatures",
				bookmarkTooltipFeaturesSerializer,
				values -> values.contains(BookmarkTooltipFeature.PREVIEW)
			)
			.setEditMode(ConfigValueEditMode.IMMEDIATE)
			.build();
		bookmarkTooltipIngredientsEnabled = bookmarkList.addBoolean("bookmarkTooltipIngredients", false)
			.addLegacyValue("tooltips", "bookmarkTooltipIngredients")
			.addLegacyValueMigration(
				"tooltips",
				"bookmarkTooltipFeatures",
				bookmarkTooltipFeaturesSerializer,
				values -> values.contains(BookmarkTooltipFeature.INGREDIENTS)
			)
			.setEditMode(ConfigValueEditMode.IMMEDIATE)
			.build();
		holdShiftToShowBookmarkTooltipFeaturesEnabled = bookmarkList.addBoolean("holdShiftToShowBookmarkTooltipFeatures", true)
			.addLegacyValue("tooltips", "holdShiftToShowBookmarkTooltipFeatures")
			.setEditMode(ConfigValueEditMode.IMMEDIATE)
			.build();

		dragDelayMs = input.addInteger("dragDelayInMilliseconds", 150, 0, 1000)
			.setEditMode(ConfigValueEditMode.IMMEDIATE)
			.build();
		smoothScrollRate = input.addInteger("smoothScrollRate", 9, 1, 50)
			.setEditMode(ConfigValueEditMode.IMMEDIATE)
			.build();
		recipeSlotCyclingEnabled = input.addBoolean("recipeSlotCyclingEnabled", true)
			.setEditMode(ConfigValueEditMode.IMMEDIATE)
			.build();

		showTagRecipesEnabled = recipes.addBoolean("showTagRecipesEnabled", true)
			.addLegacyValue("cheating", "showTagRecipesEnabled")
			.setEditMode(ConfigValueEditMode.BATCH)
			.build();
		maxRecipeGuiHeight = recipes.addInteger("recipeGuiHeight", defaultRecipeGuiHeight, minRecipeGuiHeight, maximumRecipeGuiHeight)
			.addLegacyValue("appearance", "recipeGuiHeight")
			.setEditMode(ConfigValueEditMode.IMMEDIATE)
			.build();
		IConfigListValueSerializer<RecipeSorterStage> recipeSorterStagesSerializer = LegacyEnumSerializers.list(RecipeSorterStage.class);
		recipeSortingBookmarksEnabled = recipes.addBoolean("recipeSortingBookmarks", true)
			.addLegacyValue("sorting", "recipeSortingBookmarks")
			.addLegacyValueMigration(
				"sorting",
				"recipeSorterStages",
				recipeSorterStagesSerializer,
				values -> values.contains(RecipeSorterStage.BOOKMARKED)
			)
			.setEditMode(ConfigValueEditMode.IMMEDIATE)
			.build();
		recipeSortingCraftableEnabled = recipes.addBoolean("recipeSortingCraftable", true)
			.addLegacyValue("sorting", "recipeSortingCraftable")
			.addLegacyValueMigration(
				"sorting",
				"recipeSorterStages",
				recipeSorterStagesSerializer,
				values -> values.contains(RecipeSorterStage.CRAFTABLE)
			)
			.setEditMode(ConfigValueEditMode.IMMEDIATE)
			.build();
		ingredientsSummaryEnabled = recipes.addBoolean("enableRecipesGuiIngredientsSummary", false)
			.addLegacyValue("tooltips", "enableRecipesGuiIngredientsSummary")
			.setEditMode(ConfigValueEditMode.IMMEDIATE)
			.build();

		showCreativeTabNamesEnabled = tooltips.addBoolean("showCreativeTabNamesEnabled", false)
			.setEditMode(ConfigValueEditMode.IMMEDIATE)
			.build();
		tagContentTooltipEnabled = tooltips.addBoolean("tagContentTooltipEnabled", true)
			.setEditMode(ConfigValueEditMode.IMMEDIATE)
			.build();
		hideSingleTagContentTooltipEnabled = tooltips.addBoolean("hideSingleTagContentTooltipEnabled", true)
			.setEditMode(ConfigValueEditMode.IMMEDIATE)
			.build();

		lookupFluidContentsEnabled = lookups.addBoolean("lookupFluidContentsEnabled", false)
			.setEditMode(ConfigValueEditMode.IMMEDIATE)
			.build();
		lookupBlockTagsEnabled = lookups.addBoolean("lookupBlockTagsEnabled", true)
			.setEditMode(ConfigValueEditMode.IMMEDIATE)
			.build();
		lookupHistoryEnabled = lookups.addBoolean("enabled", false)
			.addLegacyValue("lookupHistory", "enabled")
			.setEditMode(ConfigValueEditMode.IMMEDIATE)
			.build();
		maxLookupHistoryRows = lookups.addInteger("maxRows", 2, 1, 7)
			.addLegacyValue("lookupHistory", "maxRows")
			.setEditMode(ConfigValueEditMode.IMMEDIATE)
			.build();
		maxLookupHistoryIngredients = lookups.addInteger("maxIngredients", 100, 10, 1_000)
			.addLegacyValue("lookupHistory", "maxIngredients")
			.setEditMode(ConfigValueEditMode.IMMEDIATE)
			.build();
		lookupHistoryDisplaySide = lookups.addEnum("displaySide", HistoryDisplaySide.LEFT)
			.addLegacyValue("lookupHistory", "displaySide")
			.setEditMode(ConfigValueEditMode.IMMEDIATE)
			.build();

		giveMode = cheating.addEnum("giveMode", GiveMode.defaultGiveMode)
			.setEditMode(ConfigValueEditMode.IMMEDIATE)
			.build();
		cheatToHotbarUsingHotkeysEnabled = cheating.addBoolean("cheatToHotbarUsingHotkeysEnabled", false)
			.setEditMode(ConfigValueEditMode.IMMEDIATE)
			.build();
		showHiddenIngredients = cheating.addBoolean("showHiddenIngredients", false)
			.setEditMode(ConfigValueEditMode.BATCH)
			.build();

		catchRenderErrorsEnabled = advanced.addBoolean("catchRenderErrorsEnabled", !isDev)
			.setEditMode(ConfigValueEditMode.IMMEDIATE)
			.build();
		recipeSyncWarningEnabled = advanced.addBoolean("recipeSyncWarningEnabled", true)
			.setEditMode(ConfigValueEditMode.IMMEDIATE)
			.build();
		lowMemorySlowSearchEnabled = advanced.addBoolean("lowMemorySlowSearchEnabled", false)
			.addLegacyValue("performance", "lowMemorySlowSearchEnabled")
			.setEditMode(ConfigValueEditMode.BATCH)
			.build();
	}

	private static BookmarkAddPosition bookmarkAddPositionFromLegacyBoolean(boolean value) {
		if (value) {
			return BookmarkAddPosition.FRONT;
		}
		return BookmarkAddPosition.END;
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
	public IConfigValue<Boolean> showTagRecipesEnabled() {
		return showTagRecipesEnabled;
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
