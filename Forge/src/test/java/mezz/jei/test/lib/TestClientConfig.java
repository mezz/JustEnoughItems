package mezz.jei.test.lib;

import mezz.jei.api.runtime.config.IJeiConfigValue;
import mezz.jei.common.config.BookmarkTooltipFeature;
import mezz.jei.common.config.GiveMode;
import mezz.jei.common.config.HistoryDisplaySide;
import mezz.jei.common.config.IngredientSortStage;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.config.RecipeSorterStage;
import mezz.jei.common.config.file.ConfigValue;
import mezz.jei.common.config.file.IConfigListener;
import mezz.jei.common.config.file.serializers.BooleanSerializer;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class TestClientConfig implements IClientConfig {
	private final IJeiConfigValue<Boolean> recipeSlotCyclingEnabled = new ConfigValue<>(
		"test.recipeSlotCyclingEnabled",
		"recipeSlotCyclingEnabled",
		false,
		BooleanSerializer.INSTANCE
	);
	private final boolean lowMemorySlowSearchEnabled;

	public TestClientConfig(boolean lowMemorySlowSearchEnabled) {
		this.lowMemorySlowSearchEnabled = lowMemorySlowSearchEnabled;
	}

	@Override
	public boolean isCenterSearchBarEnabled() {
		return false;
	}

	@Override
	public void addCenterSearchBarEnabledListener(Consumer<Boolean> listener) {

	}

	@Override
	public void addMaxRecipeGuiHeightListener(Consumer<Integer> listener) {

	}

	@Override
	public boolean isLowMemorySlowSearchEnabled() {
		return lowMemorySlowSearchEnabled;
	}

	@Override
	public void addLowMemorySlowSearchEnabledListener(Consumer<Boolean> listener) {

	}

	@Override
	public boolean isCatchRenderErrorsEnabled() {
		return false;
	}

	@Override
	public boolean isRecipeSyncWarningEnabled() {
		return true;
	}

	@Override
	public boolean isCheatToHotbarUsingHotkeysEnabled() {
		return false;
	}

	@Override
	public boolean isAddingBookmarksToFrontEnabled() {
		return false;
	}

	@Override
	public boolean isLookupFluidContentsEnabled() {
		return false;
	}

	@Override
	public GiveMode getGiveMode() {
		return GiveMode.INVENTORY;
	}

	@Override
	public boolean getShowHiddenIngredients() {
		return false;
	}

	@Override
	public boolean isDragToRearrangeBookmarksEnabled() {
		return false;
	}

	@Override
	public boolean isLookupHistoryEnabled() {
		return false;
	}

	@Override
	public void setLookupHistoryEnabled(boolean enabled) {

	}

	@Override
	public void addLookupHistoryEnabledListener(IConfigListener<Boolean> listener) {

	}

	@Override
	public int getMaxLookupHistoryRows() {
		return 0;
	}

	@Override
	public int getMaxLookupHistoryIngredients() {
		return 0;
	}

	@Override
	public HistoryDisplaySide getLookupHistoryDisplaySide() {
		return HistoryDisplaySide.LEFT;
	}

	@Override
	public void addLookupHistoryDisplaySideListener(IConfigListener<HistoryDisplaySide> listener) {

	}

	@Override
	public boolean isIngredientsSummaryEnabled() {
		return true;
	}

	@Override
	public int getDragDelayMs() {
		return 0;
	}

	@Override
	public int getSmoothScrollRate() {
		return 9;
	}

	@Override
	public List<BookmarkTooltipFeature> getBookmarkTooltipFeatures() {
		return List.of();
	}

	@Override
	public boolean isHoldShiftToShowBookmarkTooltipFeaturesEnabled() {
		return true;
	}

	@Override
	public IJeiConfigValue<Boolean> recipeSlotCyclingEnabled() {
		return recipeSlotCyclingEnabled;
	}

	@Override
	public boolean isTagContentTooltipEnabled() {
		return true;
	}

	@Override
	public boolean getHideSingleTagContentTooltipEnabled() {
		return true;
	}

	@Override
	public boolean isShowTagRecipesEnabled() {
		return false;
	}

	@Override
	public boolean isShowCreativeTabNamesEnabled() {
		return false;
	}

	@Override
	public boolean isToastReflowEnabled() {
		return true;
	}

	@Override
	public int getMaxRecipeGuiHeight() {
		return 500;
	}

	@Override
	public List<IngredientSortStage> getIngredientSorterStages() {
		return List.of();
	}

	@Override
	public void addIngredientSorterStagesListener(Consumer<List<IngredientSortStage>> listener) {

	}

	@Override
	public Set<RecipeSorterStage> getRecipeSorterStages() {
		return Set.of();
	}

	@Override
	public void enableRecipeSorterStage(RecipeSorterStage stage) {

	}

	@Override
	public void disableRecipeSorterStage(RecipeSorterStage stage) {

	}
}
