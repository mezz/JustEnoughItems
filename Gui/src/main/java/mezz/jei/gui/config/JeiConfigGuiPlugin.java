package mezz.jei.gui.config;

import mezz.jei.api.constants.ModIds;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.common.Internal;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.config.IClientConfigs;
import mezz.jei.common.config.IClientToggleState;
import mezz.jei.common.config.IIngredientGridConfig;
import mezz.jei.common.gui.DarkModeResourcePack;
import mezz.jei.gui.config.sorting.SortingOrderConfigValues;
import mezz.jei.gui.util.CheatModeUtil;
import net.mezzdev.config.api.value.serializer.IConfigValueSerializer;
import net.mezzdev.config.gui.api.ConfigGuiPlugin;
import net.mezzdev.config.gui.api.IConfigGuiPlugin;
import net.mezzdev.config.gui.api.IConfigGuiRegistration;
import net.mezzdev.config.gui.api.IConfigScreenCategoryBuilder;
import net.mezzdev.config.gui.api.IConfigScreenBuilder;
import net.mezzdev.config.gui.api.IConfigScreenFactory;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * JEI config GUI customizations.
 */
@ConfigGuiPlugin
public class JeiConfigGuiPlugin implements IConfigGuiPlugin {
	@Nullable
	private static IConfigScreenFactory screenFactory;

	@Override
	public void onScreenFactoryAvailable(IConfigScreenFactory screenFactory) {
		JeiConfigGuiPlugin.screenFactory = screenFactory;
	}

	public static Optional<Screen> createScreen(@Nullable Screen parent) {
		IConfigScreenFactory factory = screenFactory;
		if (factory == null) {
			return Optional.empty();
		}
		return Optional.of(factory.create(parent));
	}

	@Override
	public String getModId() {
		return ModIds.JEI_ID;
	}

	@Override
	public void register(IConfigGuiRegistration registration) {
		registration.registerValueEditor(AlignmentConfigValueGuiAdapter.EDITOR_TYPE, ignored -> new AlignmentConfigValueEditor());
		registration.configureScreen(screenBuilder -> {
			screenBuilder.setTitle(Component.translatable("jei.config"));
			screenBuilder.clearDefaultCategories();
			ConfigScreenCategories categories = configureCategories(screenBuilder);
			Internal.getOptionalJeiRuntime()
				.ifPresent(runtime -> {
					configureRuntimeToggleValues(categories);
					if (Internal.getJeiFeatures().isJeiGuiEnabled()) {
						configureSortingOrderCategories(categories, runtime, JeiGuiSortingConfigRegistration.get());
					}
				});
		});
	}

	private static void configureSearchValues(IConfigScreenCategoryBuilder search, IClientConfigs clientConfigs) {
		IClientConfig clientConfig = clientConfigs.getClientConfig();
		addNestedCategory(search, "completion", "jei.config.client.search.completion")
			.addValues(List.of(
				clientConfig.searchCompletionEnabled(),
				clientConfig.maxSearchCompletionRows()
			));

		var filterConfig = clientConfigs.getIngredientFilterConfig();
		addNestedCategory(search, "matching", "jei.config.client.search.matching")
			.addValues(List.of(
				filterConfig.modNameSearchMode(),
				filterConfig.tagSearchMode(),
				filterConfig.tooltipSearchMode(),
				filterConfig.colorSearchMode(),
				filterConfig.identifierSearchMode(),
				filterConfig.creativeTabSearchMode(),
				filterConfig.searchAdvancedTooltips(),
				filterConfig.searchModIds(),
				filterConfig.searchModAliases(),
				filterConfig.searchShortModNames(),
				filterConfig.searchIngredientAliases()
			));
		addNestedCategory(search, "colors", "jei.config.colors.colors")
			.addValueByName("searchColors");
	}

	private static ConfigScreenCategories configureCategories(IConfigScreenBuilder screenBuilder) {
		IClientConfigs clientConfigs = Internal.getClientConfigs();
		IClientConfig clientConfig = clientConfigs.getClientConfig();

		IConfigScreenCategoryBuilder lists = screenBuilder.addCategory("lists");
		configureListsValues(lists, clientConfig);

		IConfigScreenCategoryBuilder ingredientList = addNestedCategory(lists, "ingredientList", "jei.config.client.ingredientList");
		configureGridValues(ingredientList, clientConfigs.getIngredientListConfig(), "jei.config.client.ingredientList.alignment");
		ingredientList.addValue(clientConfig.toastReflowEnabled());
		IConfigScreenCategoryBuilder ingredientSorting = addNestedCategory(ingredientList, "ingredientSorting", "jei.config.client.ingredientSorting")
			.addValue(clientConfig.ingredientSorterStages());

		IConfigScreenCategoryBuilder bookmarkList = addNestedCategory(lists, "bookmarkList", "jei.config.client.bookmarkList");
		configureGridValues(bookmarkList, clientConfigs.getBookmarkListConfig(), "jei.config.client.bookmarkList.alignment");

		addNestedCategory(lists, "bookmarks", "jei.config.client.bookmarks")
			.addValues(List.of(
				clientConfig.bookmarkAddPosition(),
				clientConfig.bookmarkOutputAsRecipe(),
				clientConfig.dragToRearrangeBookmarksEnabled(),
				clientConfig.bookmarkTooltipPreviewEnabled(),
				clientConfig.bookmarkTooltipIngredientsEnabled(),
				clientConfig.holdShiftToShowBookmarkTooltipFeaturesEnabled()
			));

		configureSearchValues(screenBuilder.addCategory("search"), clientConfigs);

		IConfigScreenCategoryBuilder recipes = screenBuilder.addCategory("recipes");
		addNestedCategory(recipes, "appearance", "jei.config.client.recipes.appearance")
			.addValues(List.of(
				clientConfig.maxRecipeGuiHeight(),
				clientConfig.recipeGuiWidth(),
				clientConfig.maxRecipeGuiColumns(),
				clientConfig.recipeSlotCyclingEnabled()
			));
		IConfigScreenCategoryBuilder lookups = addNestedCategory(recipes, "lookups", "jei.config.client.lookups")
			.addValues(List.of(
				clientConfig.lookupFluidContentsEnabled(),
				clientConfig.lookupBlockTagsEnabled()
			));
		addNestedCategory(lookups, "history", "jei.config.client.lookupHistory")
			.addValues(List.of(
				clientConfig.lookupHistoryEnabled(),
				clientConfig.maxLookupHistoryRows(),
				clientConfig.maxLookupHistoryColumns(),
				clientConfig.maxLookupHistoryIngredients(),
				clientConfig.lookupHistoryDisplaySide()
			));
		IConfigScreenCategoryBuilder recipeCategorySorting = addNestedCategory(recipes, "recipeCategorySorting", "jei.config.client.recipeCategorySorting")
			.addValues(List.of(
				clientConfig.recipeSortingBookmarksEnabled(),
				clientConfig.recipeSortingCraftableEnabled()
			));

		IConfigScreenCategoryBuilder tooltips = screenBuilder.addCategory("tooltips");
		addNestedCategory(tooltips, "modName", "jei.config.modIdFormat.modName")
			.addValueByName("modNameFormat");

		IConfigScreenCategoryBuilder input = screenBuilder.addCategory("input")
			.clearDefaultValues();
		addNestedCategory(input, "mouse", "jei.config.client.input.mouse")
			.addValues(List.of(
				clientConfig.guiResizeEnabled(),
				clientConfig.dragDelayMs()
			));
		addNestedCategory(input, "scrolling", "jei.config.client.input.scrolling")
			.addValues(List.of(
				clientConfig.smoothScrollingEnabled(),
				clientConfig.smoothScrollRate()
			));
		IConfigScreenCategoryBuilder keyBindings = addNestedCategory(input, "keyBindings", "jei.config.client.input.keyBindings");
		Internal.getKeyMappings().getConfigKeyMappings().stream()
			.collect(Collectors.groupingBy(KeyMapping::getCategory, LinkedHashMap::new, Collectors.toList()))
			.forEach((category, mappings) -> keyBindings.addCategory(category.id().getPath())
				.setTitle(category.label())
				.addKeyMappings(mappings));

		IConfigScreenCategoryBuilder cheating = screenBuilder.addCategory("cheating");

		IConfigScreenCategoryBuilder advanced = screenBuilder.addCategory("advanced");

		return new ConfigScreenCategories(
			ingredientList,
			bookmarkList,
			cheating,
			ingredientSorting,
			recipeCategorySorting,
			advanced
		);
	}

	private static void configureListsValues(IConfigScreenCategoryBuilder lists, IClientConfig clientConfig) {
		IConfigValueSerializer<Boolean> serializer = clientConfig.cheatToHotbarUsingHotkeysEnabled().getEditorInfo().getSerializer();
		lists.addScreenValue(new RuntimeToggleScreenValue(
			"darkModeEnabled",
			"jei.config.client.lists.darkModeEnabled",
			false,
			DarkModeResourcePack::isEnabled,
			DarkModeResourcePack::setEnabled,
			DarkModeResourcePack::addListener,
			serializer
		));
	}

	private static void configureGridValues(
		IConfigScreenCategoryBuilder category,
		IIngredientGridConfig config,
		String alignmentLocalizationKey
	) {
		category.addValue(config.maxRows())
			.addValue(config.maxColumns())
			.addScreenValue(new AlignmentConfigValueGuiAdapter(
				alignmentLocalizationKey,
				config.horizontalAlignment(),
				config.verticalAlignment()
			))
			.addValue(config.layoutMode());
	}

	private static void configureRuntimeToggleValues(ConfigScreenCategories categories) {
		IClientConfigs clientConfigs = Internal.getClientConfigs();
		IClientConfig clientConfig = clientConfigs.getClientConfig();
		IClientToggleState toggleState = Internal.getClientToggleState();
		IConfigValueSerializer<Boolean> serializer = clientConfig.cheatToHotbarUsingHotkeysEnabled().getEditorInfo().getSerializer();

		categories.ingredientList()
			.addScreenValue(new RuntimeToggleScreenValue(
				"overlaysEnabled",
				"jei.config.client.ingredientList.overlaysEnabled",
				true,
				toggleState::isOverlayEnabled,
				toggleState::setOverlayEnabled,
				toggleState::addOverlayEnabledListener,
				serializer
			));

		categories.bookmarkList()
			.addScreenValue(new RuntimeToggleScreenValue(
				"bookmarkOverlayEnabled",
				"jei.config.client.bookmarkList.bookmarkOverlayEnabled",
				true,
				toggleState::isBookmarkEnabled,
				toggleState::setBookmarkEnabled,
				toggleState::addBookmarkEnabledListener,
				serializer
			));

		categories.cheating()
			.getValueBuilder(clientConfig.giveMode())
			.insertBefore(new RuntimeToggleScreenValue(
				"cheatModeEnabled",
				"jei.config.client.cheating.cheatModeEnabled",
				false,
				toggleState::isCheatItemsEnabled,
				value -> CheatModeUtil.setCheatModeEnabled(toggleState, value),
				toggleState::addCheatItemsEnabledListener,
				serializer
			));

		categories.advanced()
			.addScreenValue(new RuntimeToggleScreenValue(
				"editModeEnabled",
				"jei.config.client.advanced.editModeEnabled",
				false,
				toggleState::isEditModeEnabled,
				toggleState::setEditModeEnabled,
				toggleState::addEditModeEnabledListener,
				serializer
			));
	}

	private static void configureSortingOrderCategories(
		ConfigScreenCategories categories,
		IJeiRuntime runtime,
		JeiGuiSortingConfigData sortingConfigData
	) {
		IClientConfigs clientConfigs = Internal.getClientConfigs();
		SortingOrderConfigValues sortingOrderConfigValues = new SortingOrderConfigValues(runtime);
		List<String> recipeCategorySortOrderValues = sortingOrderConfigValues.getRecipeCategorySortOrderValues();
		List<String> ingredientModNameSortOrderValues = sortingOrderConfigValues.getIngredientModNameSortOrderValues();
		List<String> ingredientTypeSortOrderValues = sortingOrderConfigValues.getIngredientTypeSortOrderValues();

		categories.recipeCategorySorting()
			.addStringSortingConfig(
				"recipeCategorySortOrder",
				"jei.config.client.sorting.recipeCategorySortOrder",
				clientConfigs.getRecipeCategorySortingConfig(),
				recipeCategorySortOrderValues
			)
			.setValueName(sortingOrderConfigValues::getRecipeCategorySortOrderValueName)
			.setValueDescription(SortingOrderConfigValues::getRecipeCategorySortOrderValueDescription)
			.setValueIcon(sortingOrderConfigValues::getRecipeCategorySortOrderValueIcon);

		categories.ingredientSorting()
			.addStringSortingConfig(
				"ingredientTypeSortOrder",
				"jei.config.client.sorting.ingredientTypeSortOrder",
				sortingConfigData.ingredientTypeSortingConfig(),
				ingredientTypeSortOrderValues
			)
			.setValueName(sortingOrderConfigValues::getIngredientTypeSortOrderValueName)
			.setValueDescription(SortingOrderConfigValues::getIngredientTypeSortOrderValueDescription)
			.setValueIcon(sortingOrderConfigValues::getIngredientTypeSortOrderValueIcon);

		categories.ingredientSorting()
			.addStringSortingConfig(
				"ingredientModNameSortOrder",
				"jei.config.client.sorting.ingredientModNameSortOrder",
				sortingConfigData.ingredientModNameSortingConfig(),
				ingredientModNameSortOrderValues
			)
			.setValueName(Component::literal)
			.setValueDescription(SortingOrderConfigValues::getIngredientModNameSortOrderValueDescription)
			.setValueIcon(sortingOrderConfigValues::getIngredientModNameSortOrderValueIcon);
	}

	private static IConfigScreenCategoryBuilder addNestedCategory(
		IConfigScreenCategoryBuilder parent,
		String name,
		String localizationKey
	) {
		return parent.addCategory(name)
			.setTitle(Component.translatable(localizationKey))
			.setDescription(Component.translatable(localizationKey + ".description"));
	}

	private record ConfigScreenCategories(
		IConfigScreenCategoryBuilder ingredientList,
		IConfigScreenCategoryBuilder bookmarkList,
		IConfigScreenCategoryBuilder cheating,
		IConfigScreenCategoryBuilder ingredientSorting,
		IConfigScreenCategoryBuilder recipeCategorySorting,
		IConfigScreenCategoryBuilder advanced
	) {}
}
