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

	// appearance
	private final Supplier<Boolean> centerSearchBarEnabled;
	private final Supplier<Integer> maxRecipeGuiHeight;

	// cheat_mode
	private final Supplier<GiveMode> giveMode;
	private final Supplier<Boolean> cheatToHotbarUsingHotkeysEnabled;

	// bookmarks
	private final Supplier<Boolean> addBookmarksToFrontEnabled;
	private final Supplier<Boolean> dragToRearrangeBookmarksEnabled;

	// advanced
	private final Supplier<Boolean> lowMemorySlowSearchEnabled;
	private final Supplier<Boolean> catchRenderErrorsEnabled;
	private final Supplier<Boolean> lookupFluidContentsEnabled;

	// input
	private final Supplier<Integer> dragDelayMs;

	// sorting
	private final Supplier<List<IngredientSortStage>> ingredientSorterStages;

	// tags
	private final Supplier<Boolean> hideSingleIngredientTagsEnabled;

	public ClientConfig(IConfigSchemaBuilder schema) {
		instance = this;

		boolean isDev = Services.PLATFORM.getModHelper().isInDev();

		IConfigCategoryBuilder appearance = schema.addCategory("appearance");
		centerSearchBarEnabled = appearance.addBoolean(
			"CenterSearch",
			defaultCenterSearchBar,
			"Move the JEI search bar to the bottom center of the screen."
		);
		maxRecipeGuiHeight = appearance.addInteger(
			"RecipeGuiHeight",
			defaultRecipeGuiHeight,
			minRecipeGuiHeight,
			Integer.MAX_VALUE,
			"Max recipe GUI height."
		);

		IConfigCategoryBuilder cheatMode = schema.addCategory("cheat_mode");
		giveMode = cheatMode.addEnum(
			"GiveMode",
			GiveMode.defaultGiveMode,
			"Choose if JEI should give ingredients directly to the inventory or pick them up with the mouse."
		);
		cheatToHotbarUsingHotkeysEnabled = cheatMode.addBoolean(
			"CheatToHotbarUsingHotkeysEnabled",
			false,
			"Enable cheating items into the hotbar by using Shift + numeric keys."
		);

		IConfigCategoryBuilder bookmarks = schema.addCategory("bookmarks");
		addBookmarksToFrontEnabled = bookmarks.addBoolean(
			"AddBookmarksToFrontEnabled",
			false,
			"Add new bookmarks to the front of the bookmark list instead of the end."
		);
		dragToRearrangeBookmarksEnabled = bookmarks.addBoolean(
			"DragToRearrangeBookmarksEnabled",
			true,
			"Drag bookmarks to rearrange them in the list."
		);

		IConfigCategoryBuilder advanced = schema.addCategory("advanced");
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
		lookupFluidContentsEnabled = advanced.addBoolean(
			"lookupFluidContentsEnabled",
			false,
			"When looking up recipes with items that contain fluids, also look up recipes for the fluids."
		);

		IConfigCategoryBuilder input = schema.addCategory("input");
		dragDelayMs = input.addInteger(
			"dragDelayInMilliseconds",
			150,
			0,
			1000,
			"Number of milliseconds before a long mouse click is considered a drag operation."
		);

		IConfigCategoryBuilder sorting = schema.addCategory("sorting");
		ingredientSorterStages = sorting.addList(
			"IngredientSortStages",
			IngredientSortStage.defaultStages,
			new ListSerializer<>(new EnumSerializer<>(IngredientSortStage.class)),
			"Sorting order for the ingredient list."
		);

		IConfigCategoryBuilder tags = schema.addCategory("tags");
		hideSingleIngredientTagsEnabled = tags.addBoolean(
			"HideSingleIngredientTagsEnabled",
			true,
			"Hide tags that only have 1 ingredient."
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
	public GiveMode getGiveMode() {
		return giveMode.get();
	}

	@Override
	public boolean isDragToRearrangeBookmarksEnabled() {
		return dragToRearrangeBookmarksEnabled.get();
	}

	@Override
	public int getDragDelayMs() {
		return dragDelayMs.get();
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
	public boolean isHideSingleIngredientTagsEnabled() {
		return hideSingleIngredientTagsEnabled.get();
	}
}
