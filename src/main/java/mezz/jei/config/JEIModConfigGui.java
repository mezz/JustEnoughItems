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
		ConfigCategory categoryAdvanced = Config.getConfigFile().getCategory(Config.CATEGORY_ADVANCED);
		ConfigCategory categoryInterface = Config.getConfigFile().getCategory(Config.CATEGORY_INTERFACE);
		ConfigCategory categorySearch = Config.getConfigFile().getCategory(Config.CATEGORY_SEARCH);
		ConfigCategory categoryMode = Config.getConfigFile().getCategory(Config.CATEGORY_MODE);
		ConfigCategory categoryAddons = Config.getConfigFile().getCategory(Config.CATEGORY_ADDONS);

		List<IConfigElement> configElements = new ArrayList<>();
		configElements.addAll(new ConfigElement(categoryMode).getChildElements());
		configElements.add(new ConfigElement(categoryInterface));
		configElements.add(new ConfigElement(categorySearch));
		configElements.add(new ConfigElement(categoryAdvanced));

		if (!categoryAddons.isEmpty() || categoryAddons.getChildren().size() > 0) {
			configElements.add(new ConfigElement(categoryAddons));
		}

		return configElements;
	}

	private static String getTitle(GuiScreen parent) {
		if (parent instanceof GuiModList) {
			return GuiConfig.getAbridgedConfigPath(Config.getConfigFile().toString());
		}
		return Translator.translateToLocal("config.jei.title").replace("%MODNAME", Constants.NAME);
	}
}
