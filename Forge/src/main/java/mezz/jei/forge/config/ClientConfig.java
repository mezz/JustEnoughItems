package mezz.jei.forge.config;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import mezz.jei.common.color.ColorGetter;
import mezz.jei.common.color.ColorNamer;
import mezz.jei.core.config.IClientConfig;
import mezz.jei.core.config.IngredientSortStage;
import mezz.jei.core.config.GiveMode;
import mezz.jei.core.util.function.CachedSupplierTransformer;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.EnumUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class ClientConfig implements IClientConfig {
	private static final Logger LOGGER = LogManager.getLogger();
	@Nullable
	private static IClientConfig instance;

	// Forge config
	private final ForgeConfigSpec.BooleanValue debugModeEnabled;
	private final ForgeConfigSpec.BooleanValue centerSearchBarEnabled;
	private final ForgeConfigSpec.BooleanValue lowMemorySlowSearchEnabled;
	private final ForgeConfigSpec.BooleanValue cheatToHotbarUsingHotkeysEnabled;
	private final ForgeConfigSpec.EnumValue<GiveMode> giveMode;
	private final ForgeConfigSpec.IntValue maxRecipeGuiHeight;
	private final Supplier<List<IngredientSortStage>> ingredientSorterStages;

	public ClientConfig(ForgeConfigSpec.Builder builder) {
		instance = this;

		builder.push("advanced");
		{
			builder.comment("Debug mode enabled");
			debugModeEnabled = builder.define("DebugMode", false);

			builder.comment("Display search bar in the center");
			centerSearchBarEnabled = builder.define("CenterSearch", defaultCenterSearchBar);

			builder.comment("Set low-memory mode (makes search very slow, but uses less RAM)");
			lowMemorySlowSearchEnabled = builder.define("LowMemorySlowSearchEnabled", false);

			builder.comment("Enable cheating items into the hotbar by using the shift+number keys.");
			cheatToHotbarUsingHotkeysEnabled = builder.define("CheatToHotbarUsingHotkeysEnabled", false);

			builder.comment("How items should be handed to you");
			giveMode = builder.defineEnum("GiveMode", GiveMode.defaultGiveMode);

			builder.comment("Max. recipe gui height");
			maxRecipeGuiHeight = builder.defineInRange("RecipeGuiHeight", defaultRecipeGuiHeight, minRecipeGuiHeight, Integer.MAX_VALUE);
		}
		builder.pop();

		builder.push("colors");
		{
			builder.comment("Color values to search for");
			ForgeConfigSpec.ConfigValue<List<? extends String>> configValue = builder.defineList("SearchColors", Lists.newArrayList(ColorGetter.getColorDefaults()), ClientConfig::validSearchColor);
			Supplier<Map<Integer, String>> searchColors = new CachedSupplierTransformer<>(configValue::get, ClientConfig::parseSearchColors);
			ColorNamer.create(searchColors);
		}
		builder.pop();

		builder.push("sorting");
		{
			builder.comment(String.format("Sorting order for the ingredient list. Valid stages: %s", List.of(IngredientSortStage.values())));
			List<String> defaults = IngredientSortStage.defaultStages.stream()
				.map(Enum::name)
				.toList();
			Predicate<Object> elementValidator = validEnumElement(IngredientSortStage.class);
			ForgeConfigSpec.ConfigValue<List<? extends String>> ingredientSortStages = builder.defineList("IngredientSortStages", defaults, elementValidator);
			ingredientSorterStages = new CachedSupplierTransformer<>(ingredientSortStages::get, ClientConfig::parseIngredientSorterStages);
		}
		builder.pop();
	}

	/**
	 * Only use this for hacky stuff like the debug plugin
	 */
	@Deprecated
	public static IClientConfig getInstance() {
		Preconditions.checkNotNull(instance);
		return instance;
	}

	private static List<IngredientSortStage> parseIngredientSorterStages(List<? extends String> rawStages) {
		List<IngredientSortStage> ingredientSorterStages = rawStages.stream()
			.map(s -> EnumUtils.getEnum(IngredientSortStage.class, s))
			.filter(Objects::nonNull)
			.toList();
		if (ingredientSorterStages.isEmpty()) {
			return IngredientSortStage.defaultStages;
		}
		return ingredientSorterStages;
	}

	@Override
	public boolean isDebugModeEnabled() {
		return debugModeEnabled.get();
	}

	@Override
	public boolean isCenterSearchBarEnabled() {
		return centerSearchBarEnabled.get();
	}

	@Override
	public boolean isLowMemorySlowSearchEnabled() {
		return lowMemorySlowSearchEnabled.get();
	}

	@Override
	public boolean isCheatToHotbarUsingHotkeysEnabled() {
		return cheatToHotbarUsingHotkeysEnabled.get();
	}

	@Override
	public GiveMode getGiveMode() {
		return giveMode.get();
	}

	@Override
	public int getMaxRecipeGuiHeight() {
		return maxRecipeGuiHeight.get();
	}

	@Override
	public List<IngredientSortStage> getIngredientSorterStages() {
		return ingredientSorterStages.get();
	}

	private static Map<Integer, String> parseSearchColors(List<? extends String> searchColors) {
		final ImmutableMap.Builder<Integer, String> searchColorsMapBuilder = ImmutableMap.builder();
		for (String entry : searchColors) {
			try {
				ColorEntry result = parseSearchColor(entry);
				if (result == null) {
					LOGGER.error("Invalid number format for searchColor entry: {}", entry);
				} else {
					searchColorsMapBuilder.put(result.color, result.name);
				}
			} catch (NumberFormatException e) {
				LOGGER.error("Invalid number format for searchColor entry: {}", entry, e);
			}
		}
		return searchColorsMapBuilder.build();
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

	@Nullable
	private static ColorEntry parseSearchColor(Object obj) throws NumberFormatException {
		if (obj instanceof String entry) {
			String[] values = entry.split(":");
			if (values.length == 2) {
				String name = values[0];
				Integer color = Integer.decode("0x" + values[1]);
				return new ColorEntry(color, name);
			}
		}
		return null;
	}

	private static boolean validSearchColor(Object obj) {
		try {
			var result = parseSearchColor(obj);
			return result != null;
		} catch (NumberFormatException ignored) {
			return false;
		}
	}

	private record ColorEntry(Integer color, String name) {}
}
