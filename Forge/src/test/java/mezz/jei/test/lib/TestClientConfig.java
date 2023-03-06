package mezz.jei.test.lib;

import mezz.jei.core.config.GiveMode;
import mezz.jei.core.config.IngredientSortStage;
import mezz.jei.core.config.IClientConfig;

import java.util.List;
import java.util.stream.Stream;

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
	public boolean isCheatToHotbarUsingHotkeysEnabled() {
		return false;
	}

	@Override
	public boolean isAddingBookmarksToFront() {
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
	public Stream<IngredientSortStage> getIngredientSorterStages() {
		return Stream.empty();
	}
}
