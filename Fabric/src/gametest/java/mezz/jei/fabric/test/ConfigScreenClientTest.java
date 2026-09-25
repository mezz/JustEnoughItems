package mezz.jei.fabric.test;

import mezz.jei.api.constants.ModIds;
import mezz.jei.common.Internal;
import mezz.jei.common.platform.Services;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.loader.api.FabricLoader;
import net.mezzdev.config.api.Configs;
import net.mezzdev.config.api.schema.ConfigSchemaType;
import net.mezzdev.config.api.value.IConfigValue;
import net.mezzdev.config.gui.ConfigScreen;
import net.mezzdev.config.gui.ConfigValueCategoryPath;
import net.mezzdev.config.gui.api.IConfigScreenValue;
import net.mezzdev.config.gui.api.IConfigValueEditorSerializer;
import net.mezzdev.config.gui.model.ConfigScreenModel;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@SuppressWarnings("UnstableApiUsage")
final class ConfigScreenClientTest {
	private ConfigScreenClientTest() {
	}

	static void assertCategoryLayout(Screen screen) {
		ConfigScreenModel model = getScreenModel(screen);
		Map<Object, String> valueCategories = new IdentityHashMap<>();
		Map<IConfigValue<?>, List<String>> valuePaths = new IdentityHashMap<>();
		Set<IConfigValue<?>> visibleConfigValues = Collections.newSetFromMap(new IdentityHashMap<>());
		List<String> problems = new ArrayList<>();
		for (var category : model.getCategories()) {
			for (IConfigScreenValue<?> value : category.getConfigValues()) {
				value.getConfigValue().ifPresent(visibleConfigValues::add);
				List<String> path = new ArrayList<>();
				path.add(ConfigValueCategoryPath.getMetadata(value)
					.map(ConfigValueCategoryPath::getRootCategoryName)
					.orElse(category.getName().split("/", 2)[0]));
				ConfigValueCategoryPath.getCategories(value).stream()
					.map(ConfigValueCategoryPath.Category::name)
					.forEach(path::add);
				value.getConfigValue().ifPresent(configValue -> valuePaths.put(configValue, path));
				String previousCategory = valueCategories.putIfAbsent(value.getIdentityKey(), category.getName());
				if (previousCategory != null) {
					problems.add(value.getLocalizationKey() + " appears in both " + previousCategory + " and " + category.getName());
				}
				if (value.getName().equals("horizontalAlignment") || value.getName().equals("verticalAlignment")) {
					problems.add("Separate alignment control: " + value.getLocalizationKey());
				}
			}
		}
		for (var schema : Configs.getSchemas()) {
			if (!schema.getModId().equals(ModIds.JEI_ID) || schema.getType() != ConfigSchemaType.CLIENT) {
				continue;
			}
			for (var category : schema.getCategories()) {
				if (category.getName().equals("debug")) {
					continue;
				}
				for (var value : category.getConfigValues()) {
					String name = value.getEditorInfo().getName();
					if (!name.equals("horizontalAlignment") && !name.equals("verticalAlignment") && !visibleConfigValues.contains(value)) {
						problems.add("Missing config setting: " + value.getEditorInfo().getLocalizationKey());
					}
				}
			}
		}
		for (String list : List.of("ingredientList", "bookmarkList")) {
			String key = "jei.config.client." + list + ".alignment";
			var controls = model.getAllEntryWidgets()
				.filter(widget -> widget.getConfigValue().getLocalizationKey().equals(key))
				.toList();
			if (controls.size() != 1 ||
				!(controls.getFirst().getConfigValue().getSerializer() instanceof IConfigValueEditorSerializer<?> serializer) ||
				!serializer.getEditorType().getUid().equals(Identifier.fromNamespaceAndPath(ModIds.JEI_ID, "alignment"))
			) {
				problems.add("Expected one custom alignment control for " + list + ", found " + controls.size());
			}
		}
		var clientConfigs = Internal.getClientConfigs();
		var clientConfig = clientConfigs.getClientConfig();
		var filterConfig = clientConfigs.getIngredientFilterConfig();
		Map<List<String>, List<IConfigValue<?>>> expectedGroups = Map.of(
			List.of("recipes", "appearance"), List.of(
				clientConfig.recipeSlotCyclingEnabled(), clientConfig.maxRecipeGuiHeight(),
				clientConfig.recipeGuiWidth(), clientConfig.maxRecipeGuiColumns()
			),
			List.of("input", "mouse"), List.of(clientConfig.guiResizeEnabled(), clientConfig.dragDelayMs()),
			List.of("input", "scrolling"), List.of(clientConfig.smoothScrollingEnabled(), clientConfig.smoothScrollRate()),
			List.of("lists"), List.of(clientConfigs.getIngredientListConfig().backgroundStyle()),
			List.of("search", "completion"), List.of(clientConfig.searchCompletionEnabled(), clientConfig.maxSearchCompletionRows()),
			List.of("search", "matching"), List.of(
				filterConfig.modNameSearchMode(), filterConfig.tagSearchMode(), filterConfig.tooltipSearchMode(),
				filterConfig.colorSearchMode(), filterConfig.identifierSearchMode(), filterConfig.creativeTabSearchMode(),
				filterConfig.searchAdvancedTooltips(), filterConfig.searchModIds(), filterConfig.searchModAliases(),
				filterConfig.searchShortModNames(), filterConfig.searchIngredientAliases()
			),
			List.of("recipes", "lookups"), List.of(clientConfig.lookupFluidContentsEnabled(), clientConfig.lookupBlockTagsEnabled()),
			List.of("recipes", "lookups", "history"), List.of(
				clientConfig.lookupHistoryEnabled(), clientConfig.maxLookupHistoryRows(), clientConfig.maxLookupHistoryColumns(),
				clientConfig.maxLookupHistoryIngredients(), clientConfig.lookupHistoryDisplaySide()
			),
			List.of("advanced"), List.of(
				clientConfig.catchRenderErrorsEnabled(), clientConfig.recipeSyncWarningEnabled(), clientConfig.lowMemorySlowSearchEnabled()
			)
		);
		expectedGroups.forEach((expectedPath, values) -> {
			for (var value : values) {
				List<String> actualPath = valuePaths.get(value);
				if (!expectedPath.equals(actualPath)) {
					problems.add(value.getEditorInfo().getLocalizationKey() + " belongs in " + expectedPath + ", found " + actualPath);
				}
			}
		});
		for (int i = 0; i < model.getCategories().size(); i++) {
			if (model.getCategories().get(i).getName().equals("advanced") && model.hasSubcategories(i)) {
				problems.add("Advanced must show its options without subcategories");
			}
		}
		for (var keyMapping : Internal.getKeyMappings().getConfigKeyMappings()) {
			var controls = model.getAllEntryWidgets()
				.map(widget -> widget.getConfigValue())
				.filter(value -> value.getName().equals(keyMapping.getName()))
				.toList();
			List<String> expectedSections = List.of("keyBindings", keyMapping.getCategory().id().getPath());
			if (controls.size() != 1 || !ConfigValueCategoryPath.getCategories(controls.getFirst()).stream()
				.map(ConfigValueCategoryPath.Category::name)
				.toList().equals(expectedSections)
			) {
				problems.add("Expected one grouped key binding for " + keyMapping.getName());
			}
		}
		if (Internal.getOptionalJeiRuntime().isPresent()) {
			var cheatValues = model.getCategories().stream()
				.filter(category -> category.getName().equals("cheating"))
				.flatMap(category -> category.getConfigValues().stream())
				.toList();
			if (cheatValues.isEmpty() || !cheatValues.getFirst().getName().equals("cheatModeEnabled")) {
				problems.add("Enable Cheat Mode must be the first setting in Cheat Mode");
			}
		}
		if (!problems.isEmpty()) {
			throw new AssertionError("Invalid JEI config layout:\n" + String.join("\n", problems));
		}
	}

	static void captureOrganizedCategories(ClientGameTestContext context, String screenshotName) {
		for (String categoryName : List.of("recipes", "input", "cheating", "search", "search.completion", "search.matching", "lookups", "advanced")) {
			context.runOnClient(client -> {
				Screen screen = Objects.requireNonNull(client.gui.screen());
				ConfigScreenModel model = getScreenModel(screen);
				var categories = model.getCategories();
				String categoryTitle = Component.translatable("jei.config.client." + categoryName).getString();
				for (int i = 0; i < categories.size(); i++) {
					if (categories.get(i).getLocalizedName().getString().equals(categoryTitle)) {
						model.setActiveCategoryIndex(i);
						client.gui.setScreen(screen);
						return;
					}
				}
				throw new AssertionError("Missing config category: " + categoryName);
			});
			context.takeScreenshot(screenshotName + "-" + categoryName);
		}
	}

	static void assertAlignmentSearchRenders(ClientGameTestContext context, String screenshotName) {
		EditBox search = context.computeOnClient(client -> Objects.requireNonNull(client.gui.screen()).children().stream()
			.filter(EditBox.class::isInstance)
			.map(EditBox.class::cast)
			.findFirst()
			.orElseThrow(() -> new AssertionError("Expected the config search field"))
		);
		String originalSearch = context.computeOnClient(client -> search.getValue());
		try {
			context.runOnClient(client -> {
				search.setValue("alignment");
				var visibleEntries = getScreenModel(Objects.requireNonNull(client.gui.screen())).getVisibleEntryWidgets();
				if (visibleEntries.size() != 2) {
					throw new AssertionError("Expected exactly two combined alignment search results, got " + visibleEntries.size());
				}
			});
			context.takeScreenshot(screenshotName + "-alignment");
		} finally {
			context.runOnClient(client -> search.setValue(originalSearch));
		}
	}

	private static ConfigScreenModel getScreenModel(Screen screen) {
		if (!(screen instanceof ConfigScreen)) {
			throw new AssertionError("Expected MezzConfig GUI, got " + screen.getClass().getName());
		}
		try {
			// The screen does not expose its assembled categories through the public API.
			Field model = ConfigScreen.class.getDeclaredField("model");
			model.setAccessible(true);
			return (ConfigScreenModel) model.get(screen);
		} catch (ReflectiveOperationException e) {
			throw new AssertionError("Unable to inspect the assembled config screen", e);
		}
	}

	static void run(ClientGameTestContext context) {
		if (FabricLoader.getInstance().isModLoaded("modmenu")) {
			throw new AssertionError("This test must run without Mod Menu to exercise the direct MezzConfig GUI integration");
		}
		if (!FabricLoader.getInstance().isModLoaded("mezz_config_gui")) {
			throw new AssertionError("Expected MezzConfig GUI to be installed in the client test runtime");
		}
		Screen originalScreen = context.computeOnClient(client -> client.gui.screen());
		Screen inventory = context.computeOnClient(client -> new InventoryScreen(Objects.requireNonNull(client.player)));
		try {
			context.setScreen(() -> inventory);
			context.runOnClient(client -> {
				Screen configScreen = Services.PLATFORM.getConfigHelper()
					.getConfigScreen(ModIds.JEI_ID, inventory)
					.orElseThrow(() -> new AssertionError("JEI settings must open with MezzConfig GUI installed and Mod Menu absent"));
				client.gui.setScreen(configScreen);
				assertCategoryLayout(configScreen);
				String expectedTitle = Component.translatable("jei.config").getString();
				if (!configScreen.getTitle().getString().equals(expectedTitle)) {
					throw new AssertionError("Expected JEI settings, got: " + configScreen.getTitle().getString());
				}
			});
			context.waitTick();
			context.takeScreenshot("jei-config-without-mod-menu");
			context.runOnClient(client -> {
				Objects.requireNonNull(client.gui.screen()).onClose();
				if (client.gui.screen() != inventory) {
					throw new AssertionError("Closing JEI settings must return to the inventory screen");
				}
			});
		} finally {
			context.setScreen(() -> originalScreen);
		}
	}
}
