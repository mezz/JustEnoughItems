package mezz.jei.gui.config;

import mezz.jei.api.constants.ModIds;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.common.Internal;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.config.IClientConfigs;
import mezz.jei.common.config.IClientToggleState;
import mezz.jei.gui.config.sorting.SortingOrderConfigValues;
import mezz.jei.gui.util.CheatModeUtil;
import net.mezzdev.config.api.value.serializer.IConfigValueSerializer;
import net.mezzdev.config.gui.api.ConfigGuiPlugin;
import net.mezzdev.config.gui.api.IConfigGuiPlugin;
import net.mezzdev.config.gui.api.IConfigGuiRegistration;
import net.mezzdev.config.gui.api.IConfigScreenCategoryBuilder;
import net.mezzdev.config.gui.api.IConfigScreenBuilder;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * JEI config GUI customizations.
 */
@ConfigGuiPlugin
public class JeiConfigGuiPlugin implements IConfigGuiPlugin {
	@Override
	public String getModId() {
		return ModIds.JEI_ID;
	}

	@Override
	public void register(IConfigGuiRegistration registration) {
		registration.registerValueEditor(AlignmentConfigValueGuiAdapter.EDITOR_TYPE, ignored -> new AlignmentConfigValueEditor());
		registration.configureScreen(screenBuilder -> {
			screenBuilder.setTitle(Component.translatable("jei.config"));
			screenBuilder.configureCategory("debug")
				.clearDefaultValues();
			configureAlignmentValues(screenBuilder);
			screenBuilder.configureCategory("input")
				.addKeyMappings(Internal.getKeyMappings().getConfigKeyMappings());
			Internal.getOptionalJeiRuntime()
				.ifPresent(runtime -> {
					configureRuntimeToggleValues(screenBuilder);
					if (Internal.getJeiFeatures().isJeiGuiEnabled()) {
						configureSortingOrderCategories(screenBuilder, runtime, JeiGuiSortingConfigRegistration.get());
					}
				});
		});
	}

	private static void configureAlignmentValues(IConfigScreenBuilder screenBuilder) {
		IClientConfigs clientConfigs = Internal.getClientConfigs();

		IConfigScreenCategoryBuilder ingredientList = screenBuilder.configureCategory("ingredientList");
		ingredientList.getValueBuilderByName("maxColumns")
			.insertAfter(new AlignmentConfigValueGuiAdapter(
				"jei.config.client.ingredientList.alignment",
				clientConfigs.getIngredientListConfig().horizontalAlignment(),
				clientConfigs.getIngredientListConfig().verticalAlignment()
			));
		ingredientList.getValueBuilderByName("horizontalAlignment")
			.hide();
		ingredientList.getValueBuilderByName("verticalAlignment")
			.hide();

		IConfigScreenCategoryBuilder bookmarkList = screenBuilder.configureCategory("bookmarkList");
		bookmarkList.getValueBuilderByName("maxColumns")
			.insertAfter(new AlignmentConfigValueGuiAdapter(
				"jei.config.client.bookmarkList.alignment",
				clientConfigs.getBookmarkListConfig().horizontalAlignment(),
				clientConfigs.getBookmarkListConfig().verticalAlignment()
			));
		bookmarkList.getValueBuilderByName("horizontalAlignment")
			.hide();
		bookmarkList.getValueBuilderByName("verticalAlignment")
			.hide();
	}

	private static void configureRuntimeToggleValues(IConfigScreenBuilder screenBuilder) {
		IClientConfigs clientConfigs = Internal.getClientConfigs();
		IClientConfig clientConfig = clientConfigs.getClientConfig();
		IClientToggleState toggleState = Internal.getClientToggleState();
		IConfigValueSerializer<Boolean> serializer = clientConfig.cheatToHotbarUsingHotkeysEnabled().getEditorInfo().getSerializer();

		screenBuilder.configureCategory("ingredientList")
			.addScreenValue(new RuntimeToggleScreenValue(
				"overlaysEnabled",
				"jei.config.client.ingredientList.overlaysEnabled",
				true,
				toggleState::isOverlayEnabled,
				toggleState::setOverlayEnabled,
				toggleState::addOverlayEnabledListener,
				serializer
			));

		screenBuilder.configureCategory("bookmarkList")
			.addScreenValue(new RuntimeToggleScreenValue(
				"bookmarkOverlayEnabled",
				"jei.config.client.bookmarkList.bookmarkOverlayEnabled",
				true,
				toggleState::isBookmarkEnabled,
				toggleState::setBookmarkEnabled,
				toggleState::addBookmarkEnabledListener,
				serializer
			));

		screenBuilder.configureCategory("cheating")
			.addScreenValue(new RuntimeToggleScreenValue(
				"cheatModeEnabled",
				"jei.config.client.cheating.cheatModeEnabled",
				false,
				toggleState::isCheatItemsEnabled,
				value -> CheatModeUtil.setCheatModeEnabled(toggleState, value),
				toggleState::addCheatItemsEnabledListener,
				serializer
			));

		screenBuilder.configureCategory("advanced")
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
		IConfigScreenBuilder screenBuilder,
		IJeiRuntime runtime,
		JeiGuiSortingConfigData sortingConfigData
	) {
		IClientConfigs clientConfigs = Internal.getClientConfigs();
		SortingOrderConfigValues sortingOrderConfigValues = new SortingOrderConfigValues(runtime);
		List<String> recipeCategorySortOrderValues = sortingOrderConfigValues.getRecipeCategorySortOrderValues();
		List<String> ingredientModNameSortOrderValues = sortingOrderConfigValues.getIngredientModNameSortOrderValues();
		List<String> ingredientTypeSortOrderValues = sortingOrderConfigValues.getIngredientTypeSortOrderValues();

		screenBuilder.configureCategory("recipeCategorySorting")
			.setTitle(Component.translatable("jei.config.client.recipeCategorySorting"))
			.setDescription(Component.translatable("jei.config.client.recipeCategorySorting.description"))
			.addStringSortingConfig(
				"recipeCategorySortOrder",
				"jei.config.client.sorting.recipeCategorySortOrder",
				clientConfigs.getRecipeCategorySortingConfig(),
				recipeCategorySortOrderValues
			)
			.setValueName(sortingOrderConfigValues::getRecipeCategorySortOrderValueName)
			.setValueDescription(SortingOrderConfigValues::getRecipeCategorySortOrderValueDescription)
			.setValueIcon(sortingOrderConfigValues::getRecipeCategorySortOrderValueIcon);

		IConfigScreenCategoryBuilder ingredientSorting = screenBuilder.configureCategory("ingredientSorting");
		ingredientSorting.addStringSortingConfig(
				"ingredientTypeSortOrder",
				"jei.config.client.sorting.ingredientTypeSortOrder",
				sortingConfigData.ingredientTypeSortingConfig(),
				ingredientTypeSortOrderValues
			)
			.setValueName(sortingOrderConfigValues::getIngredientTypeSortOrderValueName)
			.setValueDescription(SortingOrderConfigValues::getIngredientTypeSortOrderValueDescription)
			.setValueIcon(sortingOrderConfigValues::getIngredientTypeSortOrderValueIcon);

		ingredientSorting.addStringSortingConfig(
				"ingredientModNameSortOrder",
				"jei.config.client.sorting.ingredientModNameSortOrder",
				sortingConfigData.ingredientModNameSortingConfig(),
				ingredientModNameSortOrderValues
			)
			.setValueName(Component::literal)
			.setValueDescription(SortingOrderConfigValues::getIngredientModNameSortOrderValueDescription)
			.setValueIcon(sortingOrderConfigValues::getIngredientModNameSortOrderValueIcon);
	}
}
