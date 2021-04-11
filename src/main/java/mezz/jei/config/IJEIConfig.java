package mezz.jei.config;

import com.feed_the_beast.mods.ftbguilibrary.config.ConfigGroup;

public interface IJEIConfig {
	void buildSettingsGUI(ConfigGroup group);

	void reload();
}
