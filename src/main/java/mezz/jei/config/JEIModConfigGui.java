package mezz.jei.config;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiScreen;

import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.fml.client.GuiModList;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;

import mezz.jei.util.Translator;

public class JEIModConfigGui extends GuiConfig {

	public JEIModConfigGui(GuiScreen parent) {
		super(parent, getConfigElements(), Constants.MOD_ID, false, false, getTitle(parent));
	}

	private static List<IConfigElement> getConfigElements() {
		ConfigCategory categoryAdvanced = Config.configFile.getCategory(Config.categoryAdvanced);
		ConfigCategory categoryInterface = Config.configFile.getCategory(Config.categoryInterface);
		ConfigCategory categoryMode = Config.configFile.getCategory(Config.categoryMode);

		List<IConfigElement> configElements = new ArrayList<>();
		configElements.addAll(new ConfigElement(categoryMode).getChildElements());
		configElements.addAll(new ConfigElement(categoryInterface).getChildElements());
		configElements.add(new ConfigElement(categoryAdvanced));

		return configElements;
	}

	private static String getTitle(GuiScreen parent) {
		if (parent instanceof GuiModList) {
			return GuiConfig.getAbridgedConfigPath(Config.configFile.toString());
		}
		return Translator.translateToLocal("config.jei.title").replace("%MODNAME", Constants.NAME);
	}
}
