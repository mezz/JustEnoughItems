package mezz.jei.library.config;

import mezz.jei.common.config.ClientConfigs;

public record JeiConfigData(
	ModIdFormatConfig modIdFormatConfig,
	ColorNameConfig colorNameConfig,
	ClientConfigs clientConfigs
) {
}
