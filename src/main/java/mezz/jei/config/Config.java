package mezz.jei.config;

import javax.annotation.Nonnull;

import net.minecraft.util.StatCollector;

import net.minecraftforge.common.config.Configuration;

import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class Config {
	public static Configuration configFile;

	public static boolean cheatItemsEnabled = false;

	public static void preInit(@Nonnull FMLPreInitializationEvent event) {
		configFile = new Configuration(event.getSuggestedConfigurationFile());

		syncConfig();
	}

	public static void onConfigChanged(@Nonnull ConfigChangedEvent.OnConfigChangedEvent eventArgs) {
		if(eventArgs.modID.equals(Constants.MOD_ID))
			syncConfig();
	}

	private static void syncConfig() {
		String cheatItemsEnabledDescription = StatCollector.translateToLocal("config.jei.cheatItemsEnabled.description");
		cheatItemsEnabled = configFile.getBoolean("config.jei.cheatItemsEnabled", Configuration.CATEGORY_GENERAL, cheatItemsEnabled, cheatItemsEnabledDescription);

		if(configFile.hasChanged())
			configFile.save();
	}

}
