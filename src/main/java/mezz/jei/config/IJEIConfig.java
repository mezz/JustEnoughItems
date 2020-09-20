package mezz.jei.config;

import com.feed_the_beast.mods.ftbguilibrary.config.ConfigGroup;
import mezz.jei.api.constants.ModIds;

public interface IJEIConfig
{
	void buildSettingsGUI(ConfigGroup group);
	void reload();
}
