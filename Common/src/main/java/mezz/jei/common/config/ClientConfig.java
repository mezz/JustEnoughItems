package mezz.jei.common.config;

import com.google.common.base.Preconditions;

import mezz.jei.api.runtime.config.IJeiConfigValue;
import mezz.jei.common.config.file.IConfigCategoryBuilder;
import mezz.jei.common.config.file.IConfigSchema;
import mezz.jei.common.config.file.IConfigSchemaBuilder;
import mezz.jei.common.config.file.serializers.IngredientSortStageSerializer;
import mezz.jei.common.config.file.serializers.ListSerializer;
import mezz.jei.common.platform.Services;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class ClientConfig implements IClientConfig {
	@Nullable
	private static IClientConfig instance;
	private Optional<IConfigSchema> configSchema = Optional.empty();

	private final Supplier<Boolean> centerSearchBarEnabled;
	private final Supplier<Boolean> lowMemorySlowSearchEnabled;
	private final Supplier<Boolean> catchRenderErrorsEnabled;
	private final Supplier<Boolean> catchTooltipRenderErrorsEnabled;
	private final Supplier<Boolean> cheatToHotbarUsingHotkeysEnabled;
	private final Supplier<Boolean> addBookmarksToFrontEnabled;
	private final Supplier<Boolean> lookupFluidContentsEnabled;
	private final Supplier<Boolean> lookupBlockTagsEnabled;
	private final Supplier<GiveMode> giveMode;
	private final Supplier<Integer> maxRecipeGuiHeight;
	private final Supplier<List<IngredientSortStage>> ingredientSorterStages;

	public ClientConfig(IConfigSchemaBuilder schema) {
		instance = this;

		boolean isDev = Services.PLATFORM.getModHelper().isInDev();

		IConfigCategoryBuilder advanced = schema.addCategory("advanced");
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
		catchRenderErrorsEnabled = advanced.addBoolean(
			"CatchRenderErrorsEnabled",
			!isDev,
			"Catch render errors from ingredients and attempt to recover from them instead of crashing."
		);
		catchTooltipRenderErrorsEnabled = advanced.addBoolean(
			"CatchTooltipErrorsEnabled",
			!isDev,
			"Catch render errors from tooltips and attempt to recover from them instead of crashing."
		);
		cheatToHotbarUsingHotkeysEnabled = advanced.addBoolean(
			"CheatToHotbarUsingHotkeysEnabled",
			false,
			"Enable cheating items into the hotbar by using the shift+number keys."
		);
		addBookmarksToFrontEnabled = advanced.addBoolean(
			"AddBookmarksToFrontEnabled",
			true,
			"Enable adding new bookmarks to the front of the bookmark list."
		);
		lookupFluidContentsEnabled = advanced.addBoolean(
			"lookupFluidContentsEnabled",
			false,
			"When looking up recipes with items that contain fluids, also look up recipes for the fluids."
		);
		lookupBlockTagsEnabled = advanced.addBoolean(
			"lookupBlockTagsEnabled",
			true,
			"When searching for item tags, also include tags for the default blocks contained in the items."
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

		IConfigCategoryBuilder sorting = schema.addCategory("sorting");
		ingredientSorterStages = sorting.addList(
			"IngredientSortStages",
			IngredientSortStage.defaultStages,
			new ListSerializer<IngredientSortStage>(new IngredientSortStageSerializer()),
			"Sorting order for the ingredient list"
		);
	}

	public void setSchema(IConfigSchema schema) {
		this.configSchema = Optional.ofNullable(schema);
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
	public boolean isCenterSearchBarEnabled() {
		return centerSearchBarEnabled.get();
	}

	@Override
	public boolean isLowMemorySlowSearchEnabled() {
		return lowMemorySlowSearchEnabled.get();
	}

	@Override
	public boolean isCatchRenderErrorsEnabled() {
		return catchRenderErrorsEnabled.get();
	}

	@Override
	public boolean isCatchTooltipRenderErrorsEnabled() {
		return catchTooltipRenderErrorsEnabled.get();
	}

	@Override
	public boolean isCheatToHotbarUsingHotkeysEnabled() {
		return cheatToHotbarUsingHotkeysEnabled.get();
	}

	@Override
	public boolean isAddingBookmarksToFrontEnabled() {
		return addBookmarksToFrontEnabled.get();
	}

	@Override
	public boolean isLookupFluidContentsEnabled() {
		return lookupFluidContentsEnabled.get();
	}

	@Override
	public boolean isLookupBlockTagsEnabled() {
		return lookupBlockTagsEnabled.get();
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

	@Override
	public void setIngredientSorterStages(List<IngredientSortStage> ingredientSortStages) {
		if (configSchema.isEmpty()) {
			return;
		}
		@SuppressWarnings("unchecked")
		IJeiConfigValue<List<IngredientSortStage>> stages = (IJeiConfigValue<List<IngredientSortStage>>)configSchema.get().getConfigValue("sorting", "IngredientSortStages").orElseGet(null);
		if (stages != null) {
			stages.set(ingredientSortStages);
		}

	}

	@Override
	public String getSerializedIngredientSorterStages() {
		return ingredientSorterStages.get().stream()
			.map(o -> o.name)
			.collect(Collectors.joining(", "));
	}

	@Override
	public void setIngredientSorterStages(String ingredientSortStages) {
		if (configSchema.isEmpty()) {
			return;
		}
		@SuppressWarnings("unchecked")
		IJeiConfigValue<List<IngredientSortStage>> stages = (IJeiConfigValue<List<IngredientSortStage>>)configSchema.get().getConfigValue("sorting", "IngredientSortStages").orElseGet(null);
		if (stages != null) {
			stages.setUsingSerializedValue(ingredientSortStages);
		}

	}
}
