package mezz.jei.test.lib;

import mezz.jei.common.config.BookmarkTooltipFeature;
import mezz.jei.common.config.GiveMode;
import mezz.jei.common.config.IngredientSortStage;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.config.RecipeSorterStage;

import java.util.List;
import java.util.Set;

public class TestClientConfig implements IClientConfig {
	private final boolean lowMemorySlowSearchEnabled;

	public TestClientConfig(boolean lowMemorySlowSearchEnabled) {
		this.lowMemorySlowSearchEnabled = lowMemorySlowSearchEnabled;
	}

	@Override
	public boolean isCenterSearchBarEnabled() {
		return false;
	}

	@Override
	public boolean isLowMemorySlowSearchEnabled() {
		return lowMemorySlowSearchEnabled;
	}

	@Override
	public boolean isCatchRenderErrorsEnabled() {
		return false;
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
	public boolean isLookupBlockTagsEnabled() {
		return false;
	}

	@Override
	public GiveMode getGiveMode() {
		return GiveMode.INVENTORY;
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
	public boolean isTagContentTooltipEnabled() {
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
