package mezz.jei.config;

import javax.annotation.Nullable;
import java.io.File;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import mezz.jei.Internal;
import mezz.jei.api.constants.ModIds;
import mezz.jei.color.ColorGetter;
import mezz.jei.color.ColorNamer;
import mezz.jei.config.forge.Property;
import mezz.jei.util.GiveMode;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class ClientConfig
{
	private static final Logger LOGGER = LogManager.getLogger();
	@Nullable
	private static ClientConfig instance;
	private static final String configKeyPrefix = "config.jei";

	public static final String CATEGORY_ADVANCED = "advanced";
	public static final String CATEGORY_SEARCH_COLORS = "searchColors";

	public static final int smallestNumColumns = 4;
	public static final int largestNumColumns = 100;
	public static final int minRecipeGuiHeight = 175;
	public static final int maxRecipeGuiHeight = 5000;

	//private final LocalizedConfiguration config;
	//private final LocalizedConfiguration searchColorsConfig;

	private final ConfigValues values;

	@Deprecated
	public static ClientConfig getInstance() {
		Preconditions.checkNotNull(instance);
		return instance;
	}

	public ClientConfig(ForgeConfigSpec.Builder builder) {
		instance = this;
		//final File configFile = new File(jeiConfigurationDir, "jei.cfg");
		//final File searchColorsConfigFile = new File(jeiConfigurationDir, "searchColors.cfg");
		//this.config = new LocalizedConfiguration(configKeyPrefix, configFile, "0.4.0");
		//this.searchColorsConfig = new LocalizedConfiguration(configKeyPrefix, searchColorsConfigFile, "0.1.0");

		this.values = new ConfigValues(builder);
	}

	public boolean isDebugModeEnabled() {
		return values.debugModeEnabled.get();
	}

	public boolean isCenterSearchBarEnabled() {
		return values.centerSearchBarEnabled.get();
	}

	public GiveMode getGiveMode() {
		return values.giveMode.get();
	}

	public int getMaxColumns() {
		return values.maxColumns.get();
	}

	public int getMaxRecipeGuiHeight() {
		return values.maxRecipeGuiHeight.get();
	}

	/*public LocalizedConfiguration getConfig() {
		return config;
	}*/

	public void onPreInit() {
		syncConfig();
		syncSearchColorsConfig();
	}

	public boolean syncAllConfig() {
		boolean needsReload = false;
		if (syncConfig()) {
			needsReload = true;
		}

		if (syncSearchColorsConfig()) {
			needsReload = true;
		}

		return needsReload;
	}

	private boolean syncConfig() {
		boolean needsReload = false;
		/*
		config.addCategory(CATEGORY_ADVANCED);

		values.centerSearchBarEnabled = config.getBoolean(CATEGORY_ADVANCED, "centerSearchBarEnabled", defaultValues.centerSearchBarEnabled);

		values.giveMode = config.getEnum("giveMode", CATEGORY_ADVANCED, defaultValues.giveMode, GiveMode.values());

		values.maxColumns = config.getInt("maxColumns", CATEGORY_ADVANCED, defaultValues.maxColumns, smallestNumColumns, largestNumColumns);

		values.maxRecipeGuiHeight = config.getInt("maxRecipeGuiHeight", CATEGORY_ADVANCED, defaultValues.maxRecipeGuiHeight, minRecipeGuiHeight, maxRecipeGuiHeight);

		{
			Property property = config.get(CATEGORY_ADVANCED, "debugModeEnabled", defaultValues.debugModeEnabled);
			property.setShowInGui(false);
			values.debugModeEnabled = property.getBoolean();
		}

		final boolean configChanged = config.hasChanged();
		if (configChanged) {
			// TODO 1.13
			//config.save();
		}*/
		return needsReload;
	}

	private boolean syncSearchColorsConfig() {
		//searchColorsConfig.addCategory(CATEGORY_SEARCH_COLORS);

		final String[] searchColors = ColorGetter.getColorDefaults();
		//final String[] searchColors = searchColorsConfig.getStringList("searchColors", CATEGORY_SEARCH_COLORS, searchColorDefaults);

		final ImmutableMap.Builder<Integer, String> searchColorsMapBuilder = ImmutableMap.builder();
		for (String entry : searchColors) {
			final String[] values = entry.split(":");
			if (values.length != 2) {
				LOGGER.error("Invalid format for searchColor entry: {}", entry);
			} else {
				try {
					final String name = values[0];
					final Integer colorValue = Integer.decode("0x" + values[1]);
					searchColorsMapBuilder.put(colorValue, name);
				} catch (NumberFormatException e) {
					LOGGER.error("Invalid number format for searchColor entry: {}", entry, e);
				}
			}
		}
		final ColorNamer colorNamer = new ColorNamer(searchColorsMapBuilder.build());
		Internal.setColorNamer(colorNamer);

		/*
		final boolean configChanged = searchColorsConfig.hasChanged();
		if (configChanged) {
			// TODO 1.13
			//searchColorsConfig.save();
		}
		return configChanged;*/
		return false;
	}
}
