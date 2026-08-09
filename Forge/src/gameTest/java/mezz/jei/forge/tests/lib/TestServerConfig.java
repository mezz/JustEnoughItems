package mezz.jei.forge.tests.lib;

import mezz.jei.core.config.IServerConfig;

final class TestServerConfig implements IServerConfig {
	@Override
	public boolean isCheatModeEnabledForOp() {
		return false;
	}

	@Override
	public boolean isCheatModeEnabledForGive() {
		return false;
	}

	@Override
	public boolean isCheatModeEnabledForCreative() {
		return false;
	}
}
