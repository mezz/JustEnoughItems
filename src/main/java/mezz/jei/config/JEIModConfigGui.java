package mezz.jei.config;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiScreen;

import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.fml.client.GuiModList;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;

import mezz.jei.gui.RecipesGui;
import mezz.jei.util.Translator;

public class JEIModConfigGui extends GuiConfig {

	public JEIModConfigGui(GuiScreen parent) {
		super(getParent(parent), getConfigElements(), Constants.MOD_ID, false, false, getTitle(parent));
	}

	/** Don't return to a RecipesGui, it will not be valid after configs are changed. */
	private static GuiScreen getParent(GuiScreen parent) {
		if (parent instanceof RecipesGui) {
			return ((RecipesGui) parent).getParentScreen();
		}
		return parent;
	}

	private static List<IConfigElement> getConfigElements() {
		ConfigCategory categoryWorldConfig = Config.getWorldConfig().getCategory(SessionData.getWorldUid());
		ConfigCategory categoryAdvanced = Config.getConfig().getCategory(Config.CATEGORY_ADVANCED);
		ConfigCategory categorySearch = Config.getConfig().getCategory(Config.CATEGORY_SEARCH);

		List<IConfigElement> configElements = new ArrayList<>();
		configElements.addAll(new ConfigElement(categoryWorldConfig).getChildElements());
		configElements.addAll(new ConfigElement(categoryAdvanced).getChildElements());
		configElements.add(new ConfigElement(categorySearch));

		return configElements;
	}

	private static String getTitle(GuiScreen parent) {
		if (parent instanceof GuiModList) {
			return GuiConfig.getAbridgedConfigPath(Config.getConfig().toString());
		}
		return Translator.translateToLocal("config.jei.title").replace("%MODNAME", Constants.NAME);
	}
}
