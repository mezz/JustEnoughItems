package mezz.jei.fabric.config;

import mezz.jei.core.config.IServerConfig;
import org.jetbrains.annotations.Nullable;

// TODO: Fabric server configs
public final class ServerConfig implements IServerConfig {
	@Nullable
	private static ServerConfig INSTANCE;

	public static ServerConfig getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new ServerConfig();
		}
		return INSTANCE;
	}

	private ServerConfig() {}

	@Override
	public boolean isCheatModeEnabledForOp() {
		return true;
	}

	@Override
	public boolean isCheatModeEnabledForCreative() {
		return true;
	}

	@Override
	public boolean isCheatModeEnabledForGive() {
		return false;
	}
}
