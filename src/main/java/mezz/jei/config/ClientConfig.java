package mezz.jei.config;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import mezz.jei.Internal;
import mezz.jei.color.ColorGetter;
import mezz.jei.color.ColorNamer;
import mezz.jei.compat.Optifine;
import mezz.jei.ingredients.IngredientSortStage;
import mezz.jei.util.GiveMode;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.EnumUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class ClientConfig implements IJEIConfig, IClientConfig {
	private static final Logger LOGGER = LogManager.getLogger();
	@Nullable
	private static IClientConfig instance;

	private static final int minNumColumns = 4;
	private static final int defaultNumColumns = 9;
	private static final int largestNumColumns = 100;

	private static final int minRecipeGuiHeight = 175;
	private static final int defaultRecipeGuiHeight = 350;

	private static final GiveMode defaultGiveMode = GiveMode.MOUSE_PICKUP;
	private static final boolean defaultCenterSearchBar = false;
	private static final boolean defaultUseJeiTreeFile = false;

	public static final List<IngredientSortStage> ingredientSorterStagesDefault = Arrays.asList(
		IngredientSortStage.ITEM_TREE,
		IngredientSortStage.WEAPON_DAMAGE,
		IngredientSortStage.TOOL_TYPE,
		IngredientSortStage.ARMOR,
		IngredientSortStage.TAG,
		IngredientSortStage.ALPHABETICAL,
		IngredientSortStage.MOD_NAME,
		IngredientSortStage.INGREDIENT_TYPE,
		IngredientSortStage.CREATIVE_MENU,
		IngredientSortStage.MAX_DURABILITY
	);
	private List<IngredientSortStage> ingredientSorterStages = ingredientSorterStagesDefault;

	// Forge config
	private final ForgeConfigSpec.BooleanValue debugModeEnabled;
	private final ForgeConfigSpec.BooleanValue centerSearchBarEnabled;
	private final ForgeConfigSpec.BooleanValue lowMemorySlowSearchEnabled;
	private final ForgeConfigSpec.BooleanValue fastItemRenderingEnabled;
	private final ForgeConfigSpec.BooleanValue cheatToHotbarUsingHotkeysEnabled;
	private final ForgeConfigSpec.EnumValue<GiveMode> giveMode;
	private final ForgeConfigSpec.IntValue maxColumns;
	private final ForgeConfigSpec.IntValue maxRecipeGuiHeight;
	private final ForgeConfigSpec.ConfigValue<List<? extends String>> searchColorsCfg;
	private final ForgeConfigSpec.ConfigValue<List<? extends String>> ingredientSorterStagesCfg;
	private final ForgeConfigSpec.BooleanValue useJeiTreeFile;

	private class StageSorterConfig {
		public IngredientSortStage stage;
		public int initialWeight;
		public int requestedWeight;
		public int sortingWeight;

		public StageSorterConfig(IngredientSortStage assignStage, int weight) {
			stage = assignStage;
			initialWeight = weight;
			requestedWeight = weight;
			sortingWeight = weight;
		}
		public IngredientSortStage getStage() { return stage; }
		public int getInitialWeight() { return initialWeight;}
		public int getRequestedWeight() { return requestedWeight;}
	}

	private List<StageSorterConfig> ingredientSorterWeights;

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

			builder.comment("Enable fast batched item rendering for JEI (disable this if items in JEI are not rendering correctly)");
			fastItemRenderingEnabled = builder.define("FastItemRenderingEnabled", true);

			builder.comment("Enable cheating items into the hotbar by using the shift+number keys.");
			cheatToHotbarUsingHotkeysEnabled = builder.define("CheatToHotbarUsingHotkeysEnabled", false);
			
			builder.comment("How items should be handed to you");
			giveMode = builder.defineEnum("GiveMode", defaultGiveMode);

			builder.comment("Max number of columns shown");
			maxColumns = builder.defineInRange("MaxColumns", defaultNumColumns, minNumColumns, largestNumColumns);

			builder.comment("Max. recipe gui height");
			maxRecipeGuiHeight = builder.defineInRange("RecipeGuiHeight", defaultRecipeGuiHeight, minRecipeGuiHeight, Integer.MAX_VALUE);
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
				.collect(Collectors.toList());
			Predicate<Object> elementValidator = validEnumElement(IngredientSortStage.class);
			ingredientSorterStagesCfg = builder.defineList("IngredientSortStages", defaults, elementValidator);

			builder.comment("Force the use of the JEI InvTweaksTree.txt file vs try Inventory Tweaks's file.");
			useJeiTreeFile = builder.define("JeiSortTree", defaultUseJeiTreeFile);
		}
		builder.pop();
	}

	@Deprecated
	public static IClientConfig getInstance() {
		Preconditions.checkNotNull(instance);
		return instance;
	}

	@Override
	public void reload() {
		this.ingredientSorterStages = ingredientSorterStagesCfg.get()
			.stream()
			.map(s -> EnumUtils.getEnum(IngredientSortStage.class, s))
			.filter(Objects::nonNull)
			.collect(Collectors.toList());
		if (ingredientSorterStages.isEmpty()) {
			this.ingredientSorterStages = ingredientSorterStagesDefault;
		}

		syncSearchColorsConfig();
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
	public boolean isFastItemRenderingEnabled() {
		return fastItemRenderingEnabled.get() && !Optifine.isPresent();
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
	public int getMinColumns() {
		return minNumColumns;
	}

	@Override
	public int getMaxColumns() {
		return maxColumns.get();
	}

	@Override
	public int getMaxRecipeGuiHeight() {
		return maxRecipeGuiHeight.get();
	}

	@Override
	public List<IngredientSortStage> getIngredientSorterStages() {
		return ingredientSorterStages;
	}

	public List<String> getIngredientSorterDefaults() {
		return ingredientSorterStagesDefault.stream()
		.map(Enum::name)
		.collect(Collectors.toList());
		
	}

	public String getIngredientSorterDefaultString() {
		return ingredientSorterStagesDefault.stream()
		.map(Enum::name)
		.collect(Collectors.joining(","));
	}

	public String getIngredientSorterStagesString() {
		return String.join(",", ingredientSorterStagesCfg.get());
		//ingredientSorterStagesCfg.get().stream().collect(Collectors.joining(","));
	}

	public void setIngredientSorterStringStages(String stagesCSV) {
		List<String> stagesList = Stream.of(stagesCSV.split(",", -1)).map(s -> s.trim().toUpperCase()).collect(Collectors.toList());
		setIngredientSorterStringStages(stagesList);
	}

	public void setIngredientSorterStringStages(List<String> stagesList) {
		ingredientSorterStagesCfg.set(stagesList);
		this.ingredientSorterStages = ingredientSorterStagesCfg.get()
		.stream()
		.map(s -> EnumUtils.getEnum(IngredientSortStage.class, s))
		.filter(Objects::nonNull)
		.collect(Collectors.toList());
		if (ingredientSorterStages.isEmpty()) {
			this.ingredientSorterStages = ingredientSorterStagesDefault;
		}
		Internal.getIngredientFilter().invalidateCache();		
	}

	public void setIngredientSorterStages(List<IngredientSortStage> stagesList) {
		this.ingredientSorterStages = stagesList;
		if (ingredientSorterStages.isEmpty()) {
			this.ingredientSorterStages = ingredientSorterStagesDefault;
		}
		List<String> stagesStrings = ingredientSorterStages.stream()
		.map(Enum::name)
		.collect(Collectors.toList());
		this.ingredientSorterStagesCfg.set(stagesStrings);
		Internal.getIngredientFilter().invalidateCache();
	}
	
	public void setIngredientSorterStages(IngredientSortStage setStage, int setWeight) {
		boolean saveIt = false;
		for (StageSorterConfig sorterWeight : ingredientSorterWeights) {
			if (sorterWeight.stage == setStage) {
				sorterWeight.requestedWeight = setWeight;
				//We shouldn't try to save until we have updated the last one.
				saveIt = (sorterWeight.initialWeight == (ingredientSorterStages.size() * 10));
				break;
			}
		}

		if (saveIt) {
			Comparator<StageSorterConfig> requestedWeight = Comparator.comparing(StageSorterConfig::getRequestedWeight);
			Comparator<StageSorterConfig> initalWeight = Comparator.comparing(StageSorterConfig::getInitialWeight);

			List<IngredientSortStage> stagesList = ingredientSorterWeights.stream()
			.sorted(requestedWeight.thenComparing(initalWeight))
			.map(StageSorterConfig::getStage)
			.collect(Collectors.toList());
			setIngredientSorterStages(stagesList);
		}
	}


	@Override
	public boolean getUseJeiTreeFile() {
		return useJeiTreeFile.get();
	}

	private void syncSearchColorsConfig() {
		final ImmutableMap.Builder<Integer, String> searchColorsMapBuilder = ImmutableMap.builder();
		List<? extends String> searchColors = searchColorsCfg.get();
		for (String entry : searchColors) {
			try {
				Map.Entry<Integer, String> result = parseSearchColor(entry);
				if (result == null) {
					LOGGER.error("Invalid number format for searchColor entry: {}", entry);
				} else {
					searchColorsMapBuilder.put(result);
				}
			} catch (NumberFormatException e) {
				LOGGER.error("Invalid number format for searchColor entry: {}", entry, e);
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
			if (obj instanceof String) {
				String name = (String) obj;
				return validEntries.contains(name);
			}
			return false;
		};
	}

	@Nullable
	private static Map.Entry<Integer, String> parseSearchColor(Object obj) throws NumberFormatException {
		if (obj instanceof String) {
			String entry = (String) obj;
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
			Map.Entry<Integer, String> result = parseSearchColor(obj);
			return result != null;
		} catch (NumberFormatException ignored) {
			return false;
		}
	}

	private static class ColorEntry implements Map.Entry<Integer, String> {
		private final Integer color;
		private final String name;

		public ColorEntry(Integer color, String name) {
			this.color = color;
			this.name = name;
		}

		@Override
		public Integer getKey() {
			return color;
		}

		@Override
		public String getValue() {
			return name;
		}

		@Override
		public String setValue(String value) {
			throw new UnsupportedOperationException();
		}
	}
}
