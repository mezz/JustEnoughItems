package mezz.jei.common.config;

import mezz.jei.common.Internal;
import net.mezzdev.config.api.value.IConfigValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

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

	default boolean isCenterSearchBarEnabled() {
		return searchBarPosition().get().isCentered();
	}

	default void addCenterSearchBarEnabledListener(Consumer<SearchBarPosition> listener) {
		addListener(searchBarPosition(), listener);
	}

	default void addMaxRecipeGuiHeightListener(Consumer<Integer> listener) {
		addListener(maxRecipeGuiHeight(), listener);
	}

	default boolean isLowMemorySlowSearchEnabled() {
		return lowMemorySlowSearchEnabled().get();
	}

	default void addLowMemorySlowSearchEnabledListener(Consumer<Boolean> listener) {
		addListener(lowMemorySlowSearchEnabled(), listener);
	}

	default boolean isCatchRenderErrorsEnabled() {
		return catchRenderErrorsEnabled().get();
	}

	default boolean isRecipeSyncWarningEnabled() {
		return recipeSyncWarningEnabled().get();
	}

	default boolean isCheatToHotbarUsingHotkeysEnabled() {
		return cheatToHotbarUsingHotkeysEnabled().get();
	}

	default boolean isAddingBookmarksToFrontEnabled() {
		return bookmarkAddPosition().get().isFront();
	}

	default boolean isBookmarkOutputAsRecipeEnabled() {
		return bookmarkOutputAsRecipe().get();
	}

	default boolean isLookupFluidContentsEnabled() {
		return lookupFluidContentsEnabled().get();
	}

	default boolean isLookupBlockTagsEnabled() {
		return lookupBlockTagsEnabled().get();
	}

	default GiveMode getGiveMode() {
		return giveMode().get();
	}

	default boolean getShowHiddenIngredients() {
		return showHiddenIngredients().get();
	}

	default List<BookmarkTooltipFeature> getBookmarkTooltipFeatures() {
		List<BookmarkTooltipFeature> features = new ArrayList<>();
		if (bookmarkTooltipPreviewEnabled().get()) {
			features.add(BookmarkTooltipFeature.PREVIEW);
		}
		if (bookmarkTooltipIngredientsEnabled().get()) {
			features.add(BookmarkTooltipFeature.INGREDIENTS);
		}
		return List.copyOf(features);
	}

	default boolean isHoldShiftToShowBookmarkTooltipFeaturesEnabled() {
		return holdShiftToShowBookmarkTooltipFeaturesEnabled().get();
	}

	default boolean isDragToRearrangeBookmarksEnabled() {
		return dragToRearrangeBookmarksEnabled().get();
	}

	default boolean isLookupHistoryEnabled() {
		return lookupHistoryEnabled().get();
	}

	default void setLookupHistoryEnabled(boolean enabled) {
		lookupHistoryEnabled().set(enabled);
	}

	default void addLookupHistoryEnabledListener(Consumer<Boolean> listener) {
		addListener(lookupHistoryEnabled(), listener);
	}

	default int getMaxLookupHistoryRows() {
		return maxLookupHistoryRows().get();
	}

	default int getMaxLookupHistoryIngredients() {
		return maxLookupHistoryIngredients().get();
	}

	default HistoryDisplaySide getLookupHistoryDisplaySide() {
		return lookupHistoryDisplaySide().get();
	}

	default void addLookupHistoryDisplaySideListener(Consumer<HistoryDisplaySide> listener) {
		addListener(lookupHistoryDisplaySide(), listener);
	}

	default void addMaxLookupHistoryRowsListener(Consumer<Integer> listener) {
		addListener(maxLookupHistoryRows(), listener);
	}

	default void addMaxLookupHistoryIngredientsListener(Consumer<Integer> listener) {
		addListener(maxLookupHistoryIngredients(), listener);
	}

	default boolean isIngredientsSummaryEnabled() {
		return ingredientsSummaryEnabled().get();
	}

	default int getDragDelayMs() {
		return dragDelayMs().get();
	}

	default int getSmoothScrollRate() {
		return smoothScrollRate().get();
	}

	default int getMaxRecipeGuiHeight() {
		return maxRecipeGuiHeight().get();
	}

	default List<IngredientSortStage> getIngredientSorterStages() {
		return ingredientSorterStages().get();
	}

	default void addIngredientSorterStagesListener(Consumer<List<IngredientSortStage>> listener) {
		addListener(ingredientSorterStages(), listener);
	}

	default Set<RecipeSorterStage> getRecipeSorterStages() {
		return RecipeSorterStage.getEnabled(this);
	}

	default void enableRecipeSorterStage(RecipeSorterStage stage) {
		stage.setEnabled(this, true);
	}

	default void disableRecipeSorterStage(RecipeSorterStage stage) {
		stage.setEnabled(this, false);
	}

	default boolean isTagContentTooltipEnabled() {
		return tagContentTooltipEnabled().get();
	}

	default boolean getHideSingleTagContentTooltipEnabled() {
		return hideSingleTagContentTooltipEnabled().get();
	}

	default boolean isShowTagRecipesEnabled() {
		return showTagRecipesEnabled().get();
	}

	default boolean isShowCreativeTabNamesEnabled() {
		return showCreativeTabNamesEnabled().get();
	}

	default boolean isToastReflowEnabled() {
		return toastReflowEnabled().get();
	}

	private static <T> void addListener(IConfigValue<T> value, Consumer<T> listener) {
		Internal.registerRuntimeListenerRemoval(
			value.addListener(change -> listener.accept(change.newValue()))
		);
	}
}

