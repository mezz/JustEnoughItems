package mezz.jei.config;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.NameMap;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import mezz.jei.Internal;
import mezz.jei.color.ColorGetter;
import mezz.jei.color.ColorNamer;
import mezz.jei.ingredients.IngredientSortStage;
import mezz.jei.util.GiveMode;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.EnumUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

public final class ClientConfig implements IJEIConfig, IClientConfig {
	private static final Logger LOGGER = LogManager.getLogger();
	@Nullable
	private static IClientConfig instance;

	public static final int smallestNumColumns = 4;
	public static final int largestNumColumns = 100;
	public static final int minRecipeGuiHeight = 175;

	private final ConfigValues values;
	private List<? extends String> searchColors = Arrays.asList(ColorGetter.getColorDefaults());
	public static final List<IngredientSortStage> ingredientSorterStagesDefault = Arrays.asList(
		IngredientSortStage.MOD_NAME,
		IngredientSortStage.INGREDIENT_TYPE,
		IngredientSortStage.CREATIVE_MENU,
		IngredientSortStage.ALPHABETICAL,
		IngredientSortStage.WEAPON_DAMAGE,
		IngredientSortStage.TOOL_TYPE,
		IngredientSortStage.ARMOR,
		IngredientSortStage.TAG
	);
	private List<IngredientSortStage> ingredientSorterStages = ingredientSorterStagesDefault;

	// Forge config
	private final ForgeConfigSpec.BooleanValue debugModeEnabled;
	private final ForgeConfigSpec.BooleanValue centerSearchBarEnabled;
	private final ForgeConfigSpec.BooleanValue lowMemorySlowSearchEnabled;
	private final ForgeConfigSpec.EnumValue<GiveMode> giveMode;
	private final ForgeConfigSpec.IntValue maxColumns;
	private final ForgeConfigSpec.IntValue maxRecipeGuiHeight;
	private final ForgeConfigSpec.ConfigValue<List<? extends String>> searchColorsCfg;
	private final ForgeConfigSpec.ConfigValue<List<? extends String>> ingredientSorterStagesCfg;

	public ClientConfig(ForgeConfigSpec.Builder builder) {
		instance = this;
		this.values = new ConfigValues();
		ConfigValues defaultValues = new ConfigValues();

		builder.push("advanced");
		{
			builder.comment("Debug mode enabled");
			debugModeEnabled = builder.define("DebugMode", defaultValues.debugModeEnabled);

			builder.comment("Display search bar in the center");
			centerSearchBarEnabled = builder.define("CenterSearch", defaultValues.centerSearchBarEnabled);

			builder.comment("Set low-memory mode (makes search very slow, but uses less RAM)");
			lowMemorySlowSearchEnabled = builder.define("LowMemorySlowSearchEnabled", defaultValues.lowMemorySlowSearchEnabled);

			builder.comment("How items should be handed to you");
			giveMode = builder.defineEnum("GiveMode", defaultValues.giveMode);

			builder.comment("Max number of columns shown");
			maxColumns = builder.defineInRange("MaxColumns", defaultValues.maxColumns, smallestNumColumns, largestNumColumns);

			builder.comment("Max. recipe gui height");
			maxRecipeGuiHeight = builder.defineInRange("RecipeGuiHeight", defaultValues.maxRecipeGuiHeight, minRecipeGuiHeight, Integer.MAX_VALUE);
		}
		builder.pop();

		builder.push("colors");
		{
			builder.comment("Color values to search for");
			searchColorsCfg = builder.defineList("SearchColors", Lists.newArrayList(ColorGetter.getColorDefaults()), ClientConfig::validSearchColor);
		}
		builder.pop();

		builder.push("sorting");
		{
			builder.comment(String.format("Sorting order for the ingredient list. Valid stages: %s", Arrays.asList(IngredientSortStage.values())));
			List<String> defaults = ingredientSorterStagesDefault.stream()
				.map(Enum::name)
				.toList();
			Predicate<Object> elementValidator = validEnumElement(IngredientSortStage.class);
			ingredientSorterStagesCfg = builder.defineList("IngredientSortStages", defaults, elementValidator);
		}
		builder.pop();
	}

	@Deprecated
	public static IClientConfig getInstance() {
		Preconditions.checkNotNull(instance);
		return instance;
	}

	@Override
	public void buildSettingsGUI(ConfigGroup group) {
		ConfigValues defaultVals = new ConfigValues();

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
		return "advanced." + name;
	}

	@Override
	public void reload() {
		this.values.debugModeEnabled = debugModeEnabled.get();
		this.values.centerSearchBarEnabled = centerSearchBarEnabled.get();
		this.values.lowMemorySlowSearchEnabled = lowMemorySlowSearchEnabled.get();
		this.values.giveMode = giveMode.get();
		this.values.maxColumns = maxColumns.get();
		this.values.maxRecipeGuiHeight = maxRecipeGuiHeight.get();
		this.searchColors = searchColorsCfg.get();

		this.ingredientSorterStages = ingredientSorterStagesCfg.get()
			.stream()
			.map(s -> EnumUtils.getEnum(IngredientSortStage.class, s))
			.filter(Objects::nonNull)
			.toList();
		if (ingredientSorterStages.isEmpty()) {
			this.ingredientSorterStages = ingredientSorterStagesDefault;
		}

		syncSearchColorsConfig();
	}

	@Override
	public boolean isDebugModeEnabled() {
		return values.debugModeEnabled;
	}

	@Override
	public boolean isCenterSearchBarEnabled() {
		return values.centerSearchBarEnabled;
	}

	@Override
	public boolean isLowMemorySlowSearchEnabled() {
		return values.lowMemorySlowSearchEnabled;
	}

	@Override
	public GiveMode getGiveMode() {
		return values.giveMode;
	}

	@Override
	public int getMaxColumns() {
		return values.maxColumns;
	}

	@Override
	public int getMaxRecipeGuiHeight() {
		return values.maxRecipeGuiHeight;
	}

	@Override
	public List<IngredientSortStage> getIngredientSorterStages() {
		return ingredientSorterStages;
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

	@SuppressWarnings("SameParameterValue")
	private static Predicate<Object> validEnumElement(Class<? extends Enum<?>> enumClass) {
		Set<String> validEntries = new HashSet<>();
		Enum<?>[] enumConstants = enumClass.getEnumConstants();
		for (Enum<?> enumConstant : enumConstants) {
			String name = enumConstant.name();
			validEntries.add(name);
		}
		return obj -> {
			if (obj instanceof String name) {
				return validEntries.contains(name);
			}
			return false;
		};
	}

	private static boolean validSearchColor(Object obj) {
		if (obj instanceof String entry) {
			String[] values = entry.split(":");
			if (values.length == 2) {
				try {
					@SuppressWarnings("unused")
					Integer color = Integer.decode("0x" + values[1]);
					return true;
				} catch (NumberFormatException ignored) {
				}
			}
		}
		return false;
	}
}
