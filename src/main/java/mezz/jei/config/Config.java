package mezz.jei.config;

import javax.annotation.Nonnull;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.util.StatCollector;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class Config {
	public static Configuration configFile;

	public static boolean cheatItemsEnabled = false;
	public static boolean tooltipModNameEnabled = true;
	public static Set<String> nbtKeyBlacklist = new HashSet<>();

	public static void preInit(@Nonnull FMLPreInitializationEvent event) {
		configFile = new Configuration(event.getSuggestedConfigurationFile());

		syncConfig();
	}

	public static void onConfigChanged(@Nonnull ConfigChangedEvent.OnConfigChangedEvent eventArgs) {
		if (eventArgs.modID.equals(Constants.MOD_ID)) {
			syncConfig();
		}
	}

	private static void syncConfig() {
		String cheatItemsEnabledDescription = StatCollector.translateToLocal("config.jei.cheatItemsEnabled.description");
		cheatItemsEnabled = configFile.getBoolean("config.jei.cheatItemsEnabled", Configuration.CATEGORY_GENERAL, cheatItemsEnabled, cheatItemsEnabledDescription);

		String tooltipModNameEnabledDescription = StatCollector.translateToLocal("config.jei.tooltipModName.description");
		tooltipModNameEnabled = configFile.getBoolean("config.jei.tooltipModName", Configuration.CATEGORY_GENERAL, tooltipModNameEnabled, tooltipModNameEnabledDescription);

		String[] defaultNbtKeyBlacklist = new String[]{"BlockEntityTag", "CanPlaceOn"};
		String nbtKeyBlacklistDescription = StatCollector.translateToLocal("config.jei.nbtKeyBlacklist.description");
		String[] nbtKeyBlacklistArray = configFile.getStringList("config.jei.nbtKeyBlacklist", Configuration.CATEGORY_GENERAL, defaultNbtKeyBlacklist, nbtKeyBlacklistDescription);
		Collections.addAll(nbtKeyBlacklist, nbtKeyBlacklistArray);

		if (configFile.hasChanged()) {
			configFile.save();
		}
	}

}
