package mezz.jei.config;

import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import mezz.jei.JustEnoughItems;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.config.Configuration;

public class Config {
	public static Configuration configFile;

	public static boolean cheatItemsEnabled = true;

	public static void preInit(FMLPreInitializationEvent event) {
		configFile = new Configuration(event.getSuggestedConfigurationFile());

		syncConfig();
	}

	public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent eventArgs) {
		if(eventArgs.modID.equals(JustEnoughItems.MODID))
			syncConfig();
	}

	public static void syncConfig() {
		String cheatItemsEnabledDescription = StatCollector.translateToLocal("config.jei.cheatItemsEnabled.description");
		cheatItemsEnabled = configFile.getBoolean("config.jei.cheatItemsEnabled", Configuration.CATEGORY_GENERAL, cheatItemsEnabled, cheatItemsEnabledDescription);

		if(configFile.hasChanged())
			configFile.save();
	}

}
