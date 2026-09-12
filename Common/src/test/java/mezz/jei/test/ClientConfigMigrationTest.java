package mezz.jei.test;

import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.placement.VerticalAlignment;
import mezz.jei.common.config.BookmarkAddPosition;
import mezz.jei.common.config.ClientConfigs;
import mezz.jei.common.config.DebugConfig;
import mezz.jei.common.config.GiveMode;
import mezz.jei.common.config.HistoryDisplaySide;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.config.IIngredientFilterConfig;
import mezz.jei.common.config.IIngredientGridConfig;
import mezz.jei.common.config.IngredientGridLayoutMode;
import mezz.jei.common.config.IngredientGridNavigationMode;
import mezz.jei.common.config.IngredientSortStage;
import mezz.jei.common.config.NavigationVisibility;
import mezz.jei.common.config.SearchBarPosition;
import mezz.jei.common.config.SearchMode;
import mezz.jei.common.config.legacy.LegacyConfigPaths;
import net.mezzdev.config.file.ConfigFileWatcherSettings;
import net.mezzdev.config.file.ConfigFileUtil;
import net.mezzdev.config.file.ConfigManager;
import net.mezzdev.config.schema.ConfigSchemaBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ClientConfigMigrationTest {
	@Test
	public void loadsEveryReleasedClientConfigValue(@TempDir Path tempDir) throws IOException {
		Path configDirectory = tempDir.resolve("jei");
		UUID profileId = UUID.randomUUID();
		Path rootLegacyFile = configDirectory.resolve("jei-client.ini");
		Path legacyFile = configDirectory.resolve("players").resolve(profileId.toString()).resolve("jei-client.ini");
		Path configFile = configDirectory.resolve("client").resolve("jei-client.ini");
		Files.createDirectories(legacyFile.getParent());
		Files.writeString(rootLegacyFile, "[appearance]\ncenterSearch = false\n");
		Files.writeString(legacyFile, """
			[appearance]
			centerSearch = true
			recipeGuiHeight = 411
			toastReflowEnabled = false

			[cheating]
			giveMode = INVENTORY
			cheatToHotbarUsingHotkeysEnabled = true
			showHiddenIngredients = true
			showTagRecipesEnabled = false

			[bookmarks]
			addBookmarksToFrontEnabled = true
			bookmarkOutputAsRecipe = false
			dragToRearrangeBookmarksEnabled = false

			[tooltips]
			bookmarkTooltipFeatures = INGREDIENTS
			holdShiftToShowBookmarkTooltipFeatures = false
			showCreativeTabNamesEnabled = true
			tagContentTooltipEnabled = false
			hideSingleTagContentTooltipEnabled = false
			enableRecipesGuiIngredientsSummary = true

			[performance]
			lowMemorySlowSearchEnabled = true

			[lookups]
			lookupFluidContentsEnabled = true
			lookupBlockTagsEnabled = false

			[lookupHistory]
			enabled = true
			maxRows = 6
			maxIngredients = 321
			displaySide = RIGHT

			[advanced]
			catchRenderErrorsEnabled = false
			recipeSyncWarningEnabled = false

			[input]
			dragDelayInMilliseconds = 234
			smoothScrollRate = 17
			recipeSlotCyclingEnabled = false

			[sorting]
			ingredientSortStages = ALPHABETICAL, MOD_NAME
			recipeSorterStages = CRAFTABLE

			[search]
			modNameSearchMode = ENABLED
			tagSearchMode = DISABLED
			tooltipSearchMode = REQUIRE_PREFIX
			colorSearchMode = ENABLED
			resourceLocationSearchMode = REQUIRE_PREFIX
			creativeTabSearchMode = ENABLED
			searchAdvancedTooltips = true
			searchModIds = false
			searchModAliases = false
			searchShortModNames = true
			searchIngredientAliases = false

			[ingredientList]
			maxRows = 12
			maxColumns = 7
			horizontalAlignment = LEFT
			verticalAlignment = BOTTOM
			buttonNavigationVisibility = DISABLED
			drawBackground = true
			layoutMode = MAXIMIZE_AVAILABLE_SPACE
			navigationMode = SCROLLING

			[bookmarkList]
			maxRows = 13
			maxColumns = 6
			horizontalAlignment = RIGHT
			verticalAlignment = CENTER
			buttonNavigationVisibility = AUTO_HIDE
			drawBackground = true
			layoutMode = MAXIMIZE_AVAILABLE_SPACE
			navigationMode = SMOOTH_SCROLLING
			""");

		ConfigFileWatcherSettings disabledWatcher = ConfigFileWatcherSettings.clientDefaults().withEnabled(false);
		ConfigManager configManager = new ConfigManager("JEI Config Migration Test", disabledWatcher, disabledWatcher);
		ConfigSchemaBuilder schemaBuilder = new ConfigSchemaBuilder("jei", configFile, "jei.config.client", configManager);
		schemaBuilder.setLegacySources(LegacyConfigPaths.get(configDirectory, profileId, "jei-client.ini"));
		ClientConfigs configs = new ClientConfigs(
			schemaBuilder,
			false
		);

		IClientConfig client = configs.getClientConfig();
		assertEquals(SearchBarPosition.CENTERED, client.searchBarPosition().get());
		assertEquals(411, client.maxRecipeGuiHeight().get());
		assertFalse(client.toastReflowEnabled().get());
		assertEquals(GiveMode.INVENTORY, client.giveMode().get());
		assertTrue(client.cheatToHotbarUsingHotkeysEnabled().get());
		assertTrue(client.showHiddenIngredients().get());
		assertFalse(client.showTagRecipesEnabled().get());
		assertEquals(BookmarkAddPosition.FRONT, client.bookmarkAddPosition().get());
		assertFalse(client.bookmarkOutputAsRecipe().get());
		assertFalse(client.dragToRearrangeBookmarksEnabled().get());
		assertFalse(client.bookmarkTooltipPreviewEnabled().get());
		assertTrue(client.bookmarkTooltipIngredientsEnabled().get());
		assertFalse(client.holdShiftToShowBookmarkTooltipFeaturesEnabled().get());
		assertTrue(client.showCreativeTabNamesEnabled().get());
		assertFalse(client.tagContentTooltipEnabled().get());
		assertFalse(client.hideSingleTagContentTooltipEnabled().get());
		assertTrue(client.ingredientsSummaryEnabled().get());
		assertTrue(client.lowMemorySlowSearchEnabled().get());
		assertTrue(client.lookupFluidContentsEnabled().get());
		assertFalse(client.lookupBlockTagsEnabled().get());
		assertTrue(client.lookupHistoryEnabled().get());
		assertEquals(6, client.maxLookupHistoryRows().get());
		assertEquals(321, client.maxLookupHistoryIngredients().get());
		assertEquals(HistoryDisplaySide.RIGHT, client.lookupHistoryDisplaySide().get());
		assertFalse(client.catchRenderErrorsEnabled().get());
		assertFalse(client.recipeSyncWarningEnabled().get());
		assertEquals(234, client.dragDelayMs().get());
		assertEquals(17, client.smoothScrollRate().get());
		assertFalse(client.recipeSlotCyclingEnabled().get());
		assertEquals(List.of(IngredientSortStage.ALPHABETICAL, IngredientSortStage.MOD_NAME), client.ingredientSorterStages().get());
		assertFalse(client.recipeSortingBookmarksEnabled().get());
		assertTrue(client.recipeSortingCraftableEnabled().get());

		IIngredientFilterConfig filter = configs.getIngredientFilterConfig();
		assertEquals(SearchMode.ENABLED, filter.modNameSearchMode().get());
		assertEquals(SearchMode.DISABLED, filter.tagSearchMode().get());
		assertEquals(SearchMode.REQUIRE_PREFIX, filter.tooltipSearchMode().get());
		assertEquals(SearchMode.ENABLED, filter.colorSearchMode().get());
		assertEquals(SearchMode.REQUIRE_PREFIX, filter.resourceLocationSearchMode().get());
		assertEquals(SearchMode.ENABLED, filter.creativeTabSearchMode().get());
		assertTrue(filter.searchAdvancedTooltips().get());
		assertFalse(filter.searchModIds().get());
		assertFalse(filter.searchModAliases().get());
		assertTrue(filter.searchShortModNames().get());
		assertFalse(filter.searchIngredientAliases().get());

		assertGridConfig(
			configs.getIngredientListConfig(),
			12,
			7,
			HorizontalAlignment.LEFT,
			VerticalAlignment.BOTTOM,
			NavigationVisibility.DISABLED,
			IngredientGridLayoutMode.MAXIMIZE_AVAILABLE_SPACE,
			IngredientGridNavigationMode.SCROLLING
		);
		assertGridConfig(
			configs.getBookmarkListConfig(),
			13,
			6,
			HorizontalAlignment.RIGHT,
			VerticalAlignment.CENTER,
			NavigationVisibility.AUTO_HIDE,
			IngredientGridLayoutMode.MAXIMIZE_AVAILABLE_SPACE,
			IngredientGridNavigationMode.SMOOTH_SCROLLING
		);
		assertTrue(Files.exists(configFile));
		assertEquals(Files.readString(legacyFile), Files.readString(ConfigFileUtil.getBackupPath(legacyFile, 1)));
		assertTrue(Files.notExists(ConfigFileUtil.getBackupPath(rootLegacyFile, 1)));

		ConfigManager reloadedConfigManager = new ConfigManager("Reloaded JEI Client Config", disabledWatcher, disabledWatcher);
		ClientConfigs reloaded = new ClientConfigs(
			new ConfigSchemaBuilder("jei", configFile, "jei.config.client", reloadedConfigManager),
			false
		);
		assertFalse(reloaded.getClientConfig().bookmarkOutputAsRecipe().get());
		assertFalse(reloaded.getClientConfig().recipeSyncWarningEnabled().get());
		assertFalse(reloaded.getClientConfig().recipeSlotCyclingEnabled().get());
		assertEquals(IngredientGridLayoutMode.MAXIMIZE_AVAILABLE_SPACE, reloaded.getIngredientListConfig().layoutMode().get());
		assertEquals(IngredientGridNavigationMode.SMOOTH_SCROLLING, reloaded.getBookmarkListConfig().navigationMode().get());
	}

	private static void assertGridConfig(
		IIngredientGridConfig config,
		int maxRows,
		int maxColumns,
		HorizontalAlignment horizontalAlignment,
		VerticalAlignment verticalAlignment,
		NavigationVisibility navigationVisibility,
		IngredientGridLayoutMode layoutMode,
		IngredientGridNavigationMode navigationMode
	) {
		assertEquals(maxRows, config.maxRows().get());
		assertEquals(maxColumns, config.maxColumns().get());
		assertEquals(horizontalAlignment, config.horizontalAlignment().get());
		assertEquals(verticalAlignment, config.verticalAlignment().get());
		assertEquals(navigationVisibility, config.navigationVisibility().get());
		assertTrue(config.drawBackground().get());
		assertEquals(layoutMode, config.layoutMode().get());
		assertEquals(navigationMode, config.navigationMode().get());
	}

	@Test
	public void migratesLegacyDebugConfigTransactionallyThroughMezzConfig(@TempDir Path tempDir) throws IOException {
		Path configDirectory = tempDir.resolve("jei");
		Path legacyFile = configDirectory.resolve("jei-debug.ini");
		Path configFile = configDirectory.resolve("client").resolve("jei-debug.ini");
		Files.createDirectories(configDirectory);
		Files.writeString(legacyFile, """
			[debug]
			debugMode = true
			debugGuis = true
			debugInputs = true
			debugInfoTooltipsEnabled = true
			logSuffixTreeStats = true
			""");

		ConfigFileWatcherSettings disabledWatcher = ConfigFileWatcherSettings.clientDefaults().withEnabled(false);
		ConfigManager configManager = new ConfigManager("JEI Debug Config Migration Test", disabledWatcher, disabledWatcher);
		ConfigSchemaBuilder schemaBuilder = new ConfigSchemaBuilder("jei", configFile, "jei.config.debug", configManager);
		DebugConfig.create(schemaBuilder);
		schemaBuilder.setLegacySources(LegacyConfigPaths.get(configDirectory, UUID.randomUUID(), "jei-debug.ini"));
		schemaBuilder.build();

		assertTrue(DebugConfig.isDebugIngredientsEnabled());
		assertTrue(DebugConfig.isDebugGuisEnabled());
		assertTrue(DebugConfig.isDebugInputsEnabled());
		assertTrue(DebugConfig.isDebugInfoTooltipsEnabled());
		assertTrue(DebugConfig.isLogSuffixTreeStatsEnabled());
		assertTrue(Files.exists(configFile));
		assertEquals(Files.readString(legacyFile), Files.readString(ConfigFileUtil.getBackupPath(legacyFile, 1)));
	}
}
