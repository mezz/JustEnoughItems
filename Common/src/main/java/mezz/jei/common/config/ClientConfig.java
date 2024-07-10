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

	private final Supplier<Boolean> centerSearchBarEnabled;
	private final Supplier<Boolean> lowMemorySlowSearchEnabled;
	private final Supplier<Boolean> catchRenderErrorsEnabled;
	private final Supplier<Boolean> cheatToHotbarUsingHotkeysEnabled;
	private final Supplier<Boolean> addBookmarksToFrontEnabled;
	private final Supplier<Boolean> lookupFluidContentsEnabled;
	private final Supplier<Boolean> lookupBlockTagsEnabled;
	private final Supplier<GiveMode> giveMode;
	private final Supplier<List<BookmarkTooltipFeature>> bookmarkTooltipFeatures;
	private final Supplier<Boolean> holdShiftToShowBookmarkTooltipFeaturesEnabled;
	private final Supplier<Boolean> tagContentTooltipEnabled;
	private final Supplier<Integer> maxRecipeGuiHeight;
	private final Supplier<List<IngredientSortStage>> ingredientSorterStages;
	private final ConfigValue<List<RecipeSorterStage>> recipeSorterStages;

	public ClientConfig(IConfigSchemaBuilder schema) {
		instance = this;

		boolean isDev = Services.PLATFORM.getModHelper().isInDev();

		IConfigCategoryBuilder advanced = schema.addCategory("advanced");
		centerSearchBarEnabled = advanced.addBoolean(
			"CenterSearch",
			defaultCenterSearchBar,
			"Display search bar in the center"
		);
		lowMemorySlowSearchEnabled = advanced.addBoolean(
			"LowMemorySlowSearchEnabled",
			false,
			"Set low-memory mode (makes search very slow, but uses less RAM)"
		);
		catchRenderErrorsEnabled = advanced.addBoolean(
			"CatchRenderErrorsEnabled",
			!isDev,
			"Catch render errors from ingredients and attempt to recover from them instead of crashing."
		);
		cheatToHotbarUsingHotkeysEnabled = advanced.addBoolean(
			"CheatToHotbarUsingHotkeysEnabled",
			false,
			"Enable cheating items into the hotbar by using the shift+number keys."
		);
		addBookmarksToFrontEnabled = advanced.addBoolean(
			"AddBookmarksToFrontEnabled",
			false,
			"Add new bookmarks to the front of the bookmark list instead of the end."
		);
		lookupFluidContentsEnabled = advanced.addBoolean(
			"lookupFluidContentsEnabled",
			false,
			"When looking up recipes with items that contain fluids, also look up recipes for the fluids."
		);
		lookupBlockTagsEnabled = advanced.addBoolean(
			"lookupBlockTagsEnabled",
			true,
			"When searching for item tags, also include tags for the default blocks contained in the items."
		);
		giveMode = advanced.addEnum(
			"GiveMode",
			GiveMode.defaultGiveMode,
			"How items should be handed to you"
		);
		maxRecipeGuiHeight = advanced.addInteger(
			"RecipeGuiHeight",
			defaultRecipeGuiHeight,
			minRecipeGuiHeight,
			Integer.MAX_VALUE,
			"Max. recipe gui height"
		);

		IConfigCategoryBuilder bookmarkTooltips = schema.addCategory("bookmark_tooltips");
		bookmarkTooltipFeatures = bookmarkTooltips.addList(
			"BookmarkTooltipFeatures",
			BookmarkTooltipFeature.DEFAULT_BOOKMARK_TOOLTIP_FEATURES,
			new ListSerializer<>(new EnumSerializer<>(BookmarkTooltipFeature.class)),
			"Extra features for bookmark tooltips"
		);
		holdShiftToShowBookmarkTooltipFeaturesEnabled = bookmarkTooltips.addBoolean(
			"HoldShiftToShowBookmarkTooltipFeatures",
			true,
			"Hold shift to show bookmark tooltip features"
		);
		tagContentTooltipEnabled = bookmarkTooltips.addBoolean(
			"TagContentTooltipEnabled",
			true,
			"Show tag content in tooltip"
		);

		IConfigCategoryBuilder sorting = schema.addCategory("sorting");
		ingredientSorterStages = sorting.addList(
			"IngredientSortStages",
			IngredientSortStage.defaultStages,
			new ListSerializer<>(new EnumSerializer<>(IngredientSortStage.class)),
			"Sorting order for the ingredient list"
		);
		recipeSorterStages = sorting.addList(
			"RecipeSorterStages",
			RecipeSorterStage.defaultStages,
			new ListSerializer<>(new EnumSerializer<>(RecipeSorterStage.class)),
			"Sorting order for displayed recipes"
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
	public List<BookmarkTooltipFeature> getBookmarkTooltipFeatures() {
		return bookmarkTooltipFeatures.get();
	}

	@Override
	public boolean isHoldShiftToShowBookmarkTooltipFeaturesEnabled() {
		return holdShiftToShowBookmarkTooltipFeaturesEnabled.get();
	}

	@Override
	public boolean isTagContentTooltipEnabled() {
		return tagContentTooltipEnabled.get();
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
}
