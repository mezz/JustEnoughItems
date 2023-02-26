package mezz.jei.common.config;

import com.google.common.base.Preconditions;
import mezz.jei.common.config.file.IConfigCategoryBuilder;
import mezz.jei.common.config.file.IConfigSchemaBuilder;
import mezz.jei.common.config.file.serializers.EnumSerializer;
import mezz.jei.common.config.file.serializers.ListSerializer;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

public final class ClientConfig implements IClientConfig {
	@Nullable
	private static IClientConfig instance;

	private final Supplier<Boolean> centerSearchBarEnabled;
	private final Supplier<Boolean> lowMemorySlowSearchEnabled;
	private final Supplier<Boolean> cheatToHotbarUsingHotkeysEnabled;
	private final Supplier<Boolean> addBookmarksToFront;
	private final Supplier<GiveMode> giveMode;
	private final Supplier<Integer> maxRecipeGuiHeight;
	private final Supplier<List<IngredientSortStage>> ingredientSorterStages;

	public ClientConfig(IConfigSchemaBuilder schema) {
		instance = this;

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
		cheatToHotbarUsingHotkeysEnabled = advanced.addBoolean(
			"CheatToHotbarUsingHotkeysEnabled",
			false,
			"Enable cheating items into the hotbar by using the shift+number keys."
		);
		addBookmarksToFront = advanced.addBoolean(
			"AddBookmarksToFrontEnabled",
			true,
			"Enable adding new bookmarks to the front of the bookmark list."
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
			new ListSerializer<>(new EnumSerializer<>(IngredientSortStage.class)),
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
	public boolean isAddingBookmarksToFront() {
		return addBookmarksToFront.get();
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
