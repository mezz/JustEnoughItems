package mezz.jei.config;

import javax.annotation.Nullable;

import com.feed_the_beast.mods.ftbguilibrary.config.ConfigGroup;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigString;
import com.feed_the_beast.mods.ftbguilibrary.config.NameMap;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import mezz.jei.Internal;
import mezz.jei.color.ColorGetter;
import mezz.jei.color.ColorNamer;
import mezz.jei.util.GiveMode;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ClientConfig implements IJEIConfig
{
	private static final Logger LOGGER = LogManager.getLogger();
	@Nullable
	private static ClientConfig instance;

	public static final int smallestNumColumns = 4;
	public static final int largestNumColumns = 100;
	public static final int minRecipeGuiHeight = 175;

	private final ConfigValues values;
	private List<String> searchColors;

	// Forge config
	private final ForgeConfigSpec.BooleanValue debugModeEnabled;
	private final ForgeConfigSpec.BooleanValue centerSearchBarEnabled;
	private final ForgeConfigSpec.EnumValue<GiveMode> giveMode;
	private final ForgeConfigSpec.IntValue maxColumns;
	private final ForgeConfigSpec.IntValue maxRecipeGuiHeight;
	private final ForgeConfigSpec.ConfigValue<List<String>> searchColorsCfg;


	public ClientConfig(ForgeConfigSpec.Builder builder) {
		instance = this;
		this.values = new ConfigValues();
		ConfigValues defaultVals = new ConfigValues();

		builder.push("advanced");
		builder.comment("Debug mode enabled");
		debugModeEnabled = builder.define("DebugMode", defaultVals.debugModeEnabled);
		builder.comment("Display search bar in the center");
		centerSearchBarEnabled = builder.define("CenterSearch", defaultVals.centerSearchBarEnabled);
		builder.comment("How items should be handed to you");
		giveMode = builder.defineEnum("GiveMode", defaultVals.giveMode);
		builder.comment("Max number of columns shown");
		maxColumns = builder.defineInRange("MaxColumns", defaultVals.maxColumns, 1, Integer.MAX_VALUE);
		builder.comment("Max. recipe gui height");
		maxRecipeGuiHeight = builder.defineInRange("RecipeGuiHeight", defaultVals.maxRecipeGuiHeight, 1, Integer.MAX_VALUE);
		builder.pop();

		builder.push("colors");
		builder.comment("Color values to search for");
		searchColorsCfg = builder.define("SearchColors", Arrays.asList(ColorGetter.getColorDefaults()));
		builder.pop();
	}

	@Deprecated
	public static ClientConfig getInstance() {
		Preconditions.checkNotNull(instance);
		return instance;
	}

	@Override
	public void buildSettingsGUI(ConfigGroup group) {
		ConfigValues defaultVals = new ConfigValues();

		group.addBool(cfgTranslation("debugModeEnabled"), values.debugModeEnabled, v -> {
			debugModeEnabled.set(v);
			values.debugModeEnabled = v;
		}, defaultVals.debugModeEnabled);
		group.addBool(cfgTranslation("centerSearchBarEnabled"), values.centerSearchBarEnabled, v -> {
			centerSearchBarEnabled.set(v);
			values.centerSearchBarEnabled = v;
		}, defaultVals.centerSearchBarEnabled);
		group.addEnum(cfgTranslation("giveMode"), values.giveMode, v -> {
			giveMode.set(v);
			values.giveMode = v;
		}, NameMap.of(defaultVals.giveMode, GiveMode.values()).create());
		group.addInt(cfgTranslation("maxColumns"), values.maxColumns, v -> {
			maxColumns.set(v);
			values.maxColumns = v;
		}, defaultVals.maxColumns, 1, Integer.MAX_VALUE);
		group.addInt(cfgTranslation("maxRecipeGuiHeight"), values.maxRecipeGuiHeight, v -> {
			maxRecipeGuiHeight.set(v);
			values.maxRecipeGuiHeight = v;
		}, defaultVals.maxRecipeGuiHeight, 1, Integer.MAX_VALUE);

	}

	private String cfgTranslation(String name) {
		return "advanced."+name;
	}

	@Override
	public void reload() {
		values.debugModeEnabled = debugModeEnabled.get();
		values.centerSearchBarEnabled = centerSearchBarEnabled.get();
		values.giveMode = giveMode.get();
		values.maxColumns = maxColumns.get();
		values.maxRecipeGuiHeight = maxRecipeGuiHeight.get();
		searchColors = searchColorsCfg.get();

		syncSearchColorsConfig();
	}

	public boolean isDebugModeEnabled() {
		return values.debugModeEnabled;
	}

	public boolean isCenterSearchBarEnabled() {
		return values.centerSearchBarEnabled;
	}

	public GiveMode getGiveMode() {
		return values.giveMode;
	}

	public int getMaxColumns() {
		return values.maxColumns;
	}

	public int getMaxRecipeGuiHeight() {
		return values.maxRecipeGuiHeight;
	}

	private void syncSearchColorsConfig() {
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
	}
}
