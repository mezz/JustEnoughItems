package mezz.jei.test.lib;

import mezz.jei.common.config.GiveMode;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.config.IngredientSortStage;

import java.util.List;

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
	public GiveMode getGiveMode() {
		return GiveMode.INVENTORY;
	}

	@Override
	public boolean isShowHiddenItemsEnabled() {
		return false;
	}

	@Override
	public boolean isHoldShiftToShowBookmarkTooltipFeaturesEnabled() {
		return true;
	}

	@Override
	public boolean isDragToRearrangeBookmarksEnabled() {
		return false;
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
	public boolean isTagContentTooltipEnabled() {
		return true;
	}

	@Override
	public boolean isHideSingleIngredientTagsEnabled() {
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
	public int getMaxRecipeGuiHeight() {
		return 500;
	}

	@Override
	public List<IngredientSortStage> getIngredientSorterStages() {
		return List.of();
	}
}
