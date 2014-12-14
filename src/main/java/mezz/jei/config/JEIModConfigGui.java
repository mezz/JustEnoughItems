package mezz.jei.config;

import net.minecraft.client.gui.GuiScreen;

import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;

import cpw.mods.fml.client.config.GuiConfig;

public class JEIModConfigGui extends GuiConfig {

	@SuppressWarnings("unchecked")
	public JEIModConfigGui(GuiScreen parent) {
		super(parent,
				new ConfigElement(Config.configFile.getCategory(Configuration.CATEGORY_GENERAL)).getChildElements(),
				Constants.MOD_ID, false, false, GuiConfig.getAbridgedConfigPath(Config.configFile.toString()));
	}

}
