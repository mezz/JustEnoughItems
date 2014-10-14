package mezz.jei.config;

import cpw.mods.fml.client.config.GuiConfig;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;

public class JEIModConfigGui extends GuiConfig {

	@SuppressWarnings("unchecked")
	public JEIModConfigGui(GuiScreen parent) {
		super(parent,
				new ConfigElement(Config.configFile.getCategory(Configuration.CATEGORY_GENERAL)).getChildElements(),
				Defaults.MODID, false, false, GuiConfig.getAbridgedConfigPath(Config.configFile.toString()));
	}

}
