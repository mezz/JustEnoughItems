package mezz.jei.test.lib;

import mezz.jei.common.config.GiveMode;
import mezz.jei.common.config.IngredientSortStage;
import mezz.jei.common.config.IClientConfig;

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
	public boolean isCatchTooltipRenderErrorsEnabled() {
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
	public int getMaxRecipeGuiHeight() {
		return 500;
	}

	@Override
	public List<IngredientSortStage> getIngredientSorterStages() {
		return List.of();
	}

	@Override
	public void setIngredientSorterStages(List<IngredientSortStage> ingredientSortStages) {
	}

	@Override
	public String getSerializedIngredientSorterStages() {
		return "";
	}

	@Override
	public void setIngredientSorterStages(String ingredientSortStages) {
	}

}
