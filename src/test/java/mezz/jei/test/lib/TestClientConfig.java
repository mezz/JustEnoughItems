package mezz.jei.test.lib;

import mezz.jei.config.IClientConfig;
import mezz.jei.util.GiveMode;

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
	public GiveMode getGiveMode() {
		return GiveMode.INVENTORY;
	}

	@Override
	public int getMaxColumns() {
		return 5;
	}

	@Override
	public int getMaxRecipeGuiHeight() {
		return 500;
	}
}
