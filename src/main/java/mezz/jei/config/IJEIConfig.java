package mezz.jei.config;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;

public interface IJEIConfig {
	void buildSettingsGUI(ConfigGroup group);

	void reload();
}
