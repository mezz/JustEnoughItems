package mezz.jei.common.config;

import com.google.common.base.Preconditions;
import mezz.jei.common.color.ColorGetter;
import mezz.jei.common.color.ColorName;
import mezz.jei.common.color.ColorNamer;
import mezz.jei.common.config.file.ConfigCategoryBuilder;
import mezz.jei.common.config.file.ConfigSchemaBuilder;
import mezz.jei.common.config.file.serializers.ColorNameSerializer;
import mezz.jei.common.config.file.serializers.EnumSerializer;
import mezz.jei.core.config.GiveMode;
import mezz.jei.core.config.IClientConfig;
import mezz.jei.core.config.IngredientSortStage;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

public final class ClientConfig implements IClientConfig {
	@Nullable
	private static IClientConfig instance;

	private final Supplier<Boolean> debugModeEnabled;
	private final Supplier<Boolean> centerSearchBarEnabled;
	private final Supplier<Boolean> lowMemorySlowSearchEnabled;
	private final Supplier<Boolean> cheatToHotbarUsingHotkeysEnabled;
	private final Supplier<GiveMode> giveMode;
	private final Supplier<Integer> maxRecipeGuiHeight;
	private final Supplier<List<IngredientSortStage>> ingredientSorterStages;

	public ClientConfig(ConfigSchemaBuilder schema) {
		instance = this;

		ConfigCategoryBuilder advanced = schema.addCategory("advanced");
		debugModeEnabled = advanced.addBoolean(
			"DebugMode",
			false,
			"Debug mode enabled"
		);
		centerSearchBarEnabled = advanced.addBoolean(
			"CenterSearch",
			defaultCenterSearchBar,
			"Display search bar in the center"
		);
		lowMemorySlowSearchEnabled = advanced.addBoolean(
			"LowMemorySlowSearchEnabled",
			false,
			"Set low-memory mode (makes search very slow, but uses less RAM)"
		);
		cheatToHotbarUsingHotkeysEnabled = advanced.addBoolean(
			"CheatToHotbarUsingHotkeysEnabled",
			false,
			"Enable cheating items into the hotbar by using the shift+number keys."
		);
		giveMode = advanced.addEnum(
			"GiveMode",
			GiveMode.defaultGiveMode,
			"How items should be handed to you"
		);
		maxRecipeGuiHeight = advanced.addInteger(
			"RecipeGuiHeight",
			defaultRecipeGuiHeight,
			minRecipeGuiHeight,
			Integer.MAX_VALUE,
			"Max. recipe gui height"
		);

		ConfigCategoryBuilder colors = schema.addCategory("colors");
		Supplier<List<ColorName>> searchColors = colors.addList(
			"SearchColors",
			ColorGetter.getColorDefaults(),
			ColorNameSerializer.INSTANCE,
			"Color values to search for"
		);
		ColorNamer.create(searchColors);

		ConfigCategoryBuilder sorting = schema.addCategory("sorting");
		ingredientSorterStages = sorting.addList(
			"IngredientSortStages",
			IngredientSortStage.defaultStages,
			new EnumSerializer<>(IngredientSortStage.class),
			"Sorting order for the ingredient list"
		);
	}

	/**
	 * Only use this for hacky stuff like the debug plugin
	 */
	@Deprecated
	public static IClientConfig getInstance() {
		Preconditions.checkNotNull(instance);
		return instance;
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
}
