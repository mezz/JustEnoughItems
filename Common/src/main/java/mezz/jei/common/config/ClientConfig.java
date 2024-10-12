package mezz.jei.common.config;

import com.google.common.base.Preconditions;
import mezz.jei.common.config.file.IConfigCategoryBuilder;
import mezz.jei.common.config.file.IConfigSchemaBuilder;
import mezz.jei.common.config.file.serializers.EnumSerializer;
import mezz.jei.common.config.file.serializers.ListSerializer;
import mezz.jei.common.platform.Services;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

public final class ClientConfig implements IClientConfig {
	@Nullable
	private static IClientConfig instance;

	private final Supplier<Boolean> centerSearchBarEnabled;
	private final Supplier<Boolean> lowMemorySlowSearchEnabled;
	private final Supplier<Boolean> catchRenderErrorsEnabled;
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
			"Move the JEI search bar to the bottom center of the screen."
		);
		lowMemorySlowSearchEnabled = advanced.addBoolean(
			"LowMemorySlowSearchEnabled",
			false,
			"Set low-memory mode (makes search very slow but uses less RAM)."
		);
		catchRenderErrorsEnabled = advanced.addBoolean(
			"CatchRenderErrorsEnabled",
			!isDev,
			"Catch render errors from ingredients and attempt to recover from them instead of crashing."
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
			"Choose if JEI should give ingredients directly to the inventory or pick them up with the mouse."
		);
		maxRecipeGuiHeight = advanced.addInteger(
			"RecipeGuiHeight",
			defaultRecipeGuiHeight,
			minRecipeGuiHeight,
			Integer.MAX_VALUE,
			"Max recipe GUI height."
		);

		IConfigCategoryBuilder sorting = schema.addCategory("sorting");
		ingredientSorterStages = sorting.addList(
			"IngredientSortStages",
			IngredientSortStage.defaultStages,
			new ListSerializer<>(new EnumSerializer<>(IngredientSortStage.class)),
			"Sorting order for the ingredient list."
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
}
