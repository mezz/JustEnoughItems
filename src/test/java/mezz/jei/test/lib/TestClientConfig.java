package mezz.jei.test.lib;

import mezz.jei.config.ClientConfig;
import mezz.jei.config.IClientConfig;
import mezz.jei.ingredients.IngredientSortStage;
import mezz.jei.util.GiveMode;

import java.util.List;

public class TestClientConfig implements IClientConfig {
	private final boolean lowMemorySlowSearchEnabled;

	public TestClientConfig(boolean lowMemorySlowSearchEnabled) {
		this.lowMemorySlowSearchEnabled = lowMemorySlowSearchEnabled;
	}

	@Override
	public boolean isDebugModeEnabled() {
		return false;
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
	public boolean isCheatToHotbarUsingHotkeysEnabled() {
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
		return ClientConfig.ingredientSorterStagesDefault;
	}
}
