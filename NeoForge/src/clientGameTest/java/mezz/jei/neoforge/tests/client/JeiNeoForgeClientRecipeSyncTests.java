package mezz.jei.neoforge.tests.client;

import mezz.jei.common.Internal;
import mezz.jei.common.network.ClientConnectionHelper;
import mezz.jei.test.lib.JUnitXmlTestReporter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Runs client-side recipe sync tests against disposable external servers.
 */
public final class JeiNeoForgeClientRecipeSyncTests {
	private static final String TEST_CASE_PROPERTY = "jei.clientRecipeSyncTest";
	private static final String ALL_TEST_CASES = "all";
	private static final String JUNIT_SUITE_NAME = "neoforge-client-recipe-sync";
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Duration ASSERTION_TIMEOUT = Duration.ofSeconds(60);
	private static final Duration WORLD_LOAD_TIMEOUT = Duration.ofSeconds(120);
	private static final AtomicBoolean STARTED = new AtomicBoolean(false);
	private static final ResourceKey<Recipe<?>> CRAFTING_TABLE_RECIPE_KEY = ResourceKey.create(Registries.RECIPE, Identifier.withDefaultNamespace("crafting_table"));

	private JeiNeoForgeClientRecipeSyncTests() {

	}

	public static void register() {
		NeoForge.EVENT_BUS.addListener(JeiNeoForgeClientRecipeSyncTests::onClientTick);
	}

	private static void onClientTick(ClientTickEvent.Post event) {
		if (STARTED.compareAndSet(false, true)) {
			Thread thread = new Thread(JeiNeoForgeClientRecipeSyncTests::runTests, "JEI NeoForge Client Recipe Sync Tests");
			thread.setDaemon(false);
			thread.start();
		}
	}

	private static void runTests() {
		int exitCode = 0;
		String testName = TestCase.fromSystemPropertyId();
		try {
			for (TestCase currentTestCase : TestCase.fromSystemProperty()) {
				testName = currentTestCase.displayName;
				JUnitXmlTestReporter.runAndReport(
					JUNIT_SUITE_NAME,
					currentTestCase.id,
					currentTestCase::run
				);
				LOGGER.info("JEI NeoForge client recipe sync test passed: {}", currentTestCase.displayName);
			}
		} catch (Throwable t) {
			exitCode = 1;
			LOGGER.error("JEI NeoForge client recipe sync test failed: {}", testName, t);
		} finally {
			stopClient(exitCode);
		}
	}

	private static void runSingleplayerTestCase(String name, Runnable assertions) {
		LOGGER.info("Starting JEI NeoForge client recipe sync test: {}", name);
		try (SingleplayerWorld ignored = SingleplayerWorld.create()) {
			assertions.run();
		}
		assertJeiClientStateCleared(name);
		LOGGER.info("Finished JEI NeoForge client recipe sync test: {}", name);
	}

	private static void runTestCase(String name, NeoForgeExternalTestServer server, Runnable assertions) {
		LOGGER.info("Starting JEI NeoForge client recipe sync test: {}", name);
		try (NeoForgeExternalTestServer.Connection connection = server.connect()
		) {
			assertions.run();
		}
		assertJeiClientStateCleared(name);
		LOGGER.info("Finished JEI NeoForge client recipe sync test: {}", name);
	}

	private static void assertSyncedRecipesFromSingleplayer() {
		ClientTestUtil.waitUntil(
			() -> ClientTestUtil.computeOnClient(client -> hasJeiRuntime() &&
				client.hasSingleplayerServer() &&
				Internal.hasClientSyncedRecipes() &&
				hasVanillaRecipes(Internal.getClientSyncedRecipes()) &&
				Internal.getServerConnection().isJeiOnServer() &&
				Internal.getServerConnection().isSameModLoader()),
			ASSERTION_TIMEOUT,
			() -> "Expected JEI to detect the integrated NeoForge server with JEI, and use synced recipes. " + describeRecipeState()
		);
	}

	private static void assertSyncedRecipesFromJeiServer() {
		ClientTestUtil.waitUntil(
			() -> ClientTestUtil.computeOnClient(client -> hasJeiRuntime() &&
				Internal.hasClientSyncedRecipes() &&
				hasVanillaRecipes(Internal.getClientSyncedRecipes()) &&
				Internal.getServerConnection().isJeiOnServer() &&
				Internal.getServerConnection().isSameModLoader()),
			ASSERTION_TIMEOUT,
			() -> "Expected JEI to detect a server with the client's mod loader and JEI, and use synced recipes. " + describeRecipeState()
		);
	}

	private static void assertFallbackRecipesFromVanillaServer() {
		ClientTestUtil.waitUntil(
			() -> ClientTestUtil.computeOnClient(client -> hasJeiRuntime() &&
				!Internal.hasClientSyncedRecipes() &&
				hasVanillaRecipes(Internal.getClientSyncedRecipes()) &&
				!Internal.getServerConnection().isJeiOnServer() &&
				!Internal.getServerConnection().isSameModLoader()),
			ASSERTION_TIMEOUT,
			() -> "Expected JEI to detect a server without the client's mod loader and use fallback vanilla recipes. " + describeRecipeState()
		);
	}

	private static void assertFallbackRecipesFromServerWithoutJei() {
		ClientTestUtil.waitUntil(
			() -> ClientTestUtil.computeOnClient(client -> hasJeiRuntime() &&
				!Internal.hasClientSyncedRecipes() &&
				hasVanillaRecipes(Internal.getClientSyncedRecipes()) &&
				!Internal.getServerConnection().isJeiOnServer() &&
				Internal.getServerConnection().isSameModLoader()),
			ASSERTION_TIMEOUT,
			() -> "Expected JEI to detect a server with the client's mod loader but without JEI and use fallback vanilla recipes. " + describeRecipeState()
		);
	}

	private static boolean hasVanillaRecipes(RecipeMap recipeMap) {
		return !recipeMap.values().isEmpty() &&
			recipeMap.byKey(CRAFTING_TABLE_RECIPE_KEY) != null;
	}

	private static void assertJeiClientStateCleared(String name) {
		ClientTestUtil.waitUntil(
			() -> ClientTestUtil.computeOnClient(client ->
				!hasJeiRuntime() &&
					!Internal.hasClientRecipes() &&
					!Internal.hasClientSyncedRecipes() &&
					!Internal.hasClientFallbackRecipes() &&
					Internal.getClientSyncedRecipes().values().isEmpty() &&
					!Internal.getServerConnection().isJeiOnServer() &&
					!Internal.getServerConnection().isSameModLoader()),
			ASSERTION_TIMEOUT,
			() -> "Expected JEI client state to be cleared after disconnecting from " + name + ". " + describeJeiClientState()
		);
	}

	private static String describeJeiClientState() {
		return ClientTestUtil.computeOnClient(client -> {
			boolean hasRuntime = hasJeiRuntime();
			boolean hasClientRecipes = Internal.hasClientRecipes();
			boolean hasSyncedRecipes = Internal.hasClientSyncedRecipes();
			boolean hasFallbackRecipes = Internal.hasClientFallbackRecipes();
			RecipeMap recipes = Internal.getClientSyncedRecipes();
			return "runtime=" + hasRuntime +
				", hasClientRecipes=" + hasClientRecipes +
				", synced=" + hasSyncedRecipes +
				", fallback=" + hasFallbackRecipes +
				", recipeCount=" + recipes.values().size() +
				", hasCraftingTable=" + (recipes.byKey(CRAFTING_TABLE_RECIPE_KEY) != null) +
				", jeiOnServer=" + Internal.getServerConnection().isJeiOnServer() +
				", sameModLoader=" + Internal.getServerConnection().isSameModLoader() +
				", serverBrand=" + ClientConnectionHelper.getServerBrand();
		});
	}

	private static String describeRecipeState() {
		return ClientTestUtil.computeOnClient(client -> {
			boolean hasRuntime = hasJeiRuntime();
			boolean hasSyncedRecipes = Internal.hasClientSyncedRecipes();
			RecipeMap recipes = Internal.getClientSyncedRecipes();
			return "runtime=" + hasRuntime +
				", synced=" + hasSyncedRecipes +
				", recipeCount=" + recipes.values().size() +
				", hasCraftingTable=" + (recipes.byKey(CRAFTING_TABLE_RECIPE_KEY) != null) +
				", jeiOnServer=" + Internal.getServerConnection().isJeiOnServer() +
				", sameModLoader=" + Internal.getServerConnection().isSameModLoader() +
				", serverBrand=" + ClientConnectionHelper.getServerBrand() +
				", hasLevel=" + (client.level != null) +
				", hasSingleplayerServer=" + client.hasSingleplayerServer();
		});
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	private static boolean hasJeiRuntime() {
		try {
			Internal.getJeiRuntime();
			return true;
		} catch (IllegalStateException ignored) {
			return false;
		}
	}

	private static void stopClient(int exitCode) {
		try {
			ClientTestUtil.runOnClient(Minecraft::stop);
		} catch (Throwable t) {
			exitCode = 1;
			LOGGER.error("Failed to stop Minecraft after JEI NeoForge client recipe sync tests.", t);
		}
		if (exitCode != 0) {
			System.exit(exitCode);
		}
	}

	private enum TestCase {
		SINGLEPLAYER("singleplayer", "NeoForge singleplayer") {
			@Override
			public void run() {
				runSingleplayerTestCase(displayName(), JeiNeoForgeClientRecipeSyncTests::assertSyncedRecipesFromSingleplayer);
			}
		},
		NEOFORGE_SERVER_WITH_JEI("neoforgeServerWithJei", "NeoForge server with JEI") {
			@Override
			public void run() {
				try (NeoForgeExternalTestServer server = NeoForgeExternalTestServer.startNeoForgeWithJei()) {
					runTestCase(displayName(), server, JeiNeoForgeClientRecipeSyncTests::assertSyncedRecipesFromJeiServer);
				}
			}
		},
		NEOFORGE_SERVER_WITHOUT_JEI("neoforgeServerWithoutJei", "NeoForge server without JEI") {
			@Override
			public void run() {
				try (NeoForgeExternalTestServer server = NeoForgeExternalTestServer.startNeoForgeWithoutJei()) {
					runTestCase(displayName(), server, JeiNeoForgeClientRecipeSyncTests::assertFallbackRecipesFromServerWithoutJei);
				}
			}
		},
		VANILLA_SERVER_WITHOUT_JEI("vanillaServerWithoutJei", "vanilla server without JEI") {
			@Override
			public void run() {
				try (NeoForgeExternalTestServer server = NeoForgeExternalTestServer.startVanilla()) {
					runTestCase(displayName(), server, JeiNeoForgeClientRecipeSyncTests::assertFallbackRecipesFromVanillaServer);
				}
			}
		};

		private final String id;
		private final String displayName;

		TestCase(String id, String displayName) {
			this.id = id;
			this.displayName = displayName;
		}

		public abstract void run();

		protected String displayName() {
			return displayName;
		}

		public static List<TestCase> fromSystemProperty() {
			String id = fromSystemPropertyId();
			if (ALL_TEST_CASES.equals(id)) {
				return Arrays.asList(values());
			}
			return Arrays.stream(values())
				.filter(testCase -> testCase.id.equals(id))
				.findFirst()
				.map(List::of)
				.orElseThrow(() -> new IllegalArgumentException(
					"Unknown JEI client recipe sync test case '" + id + "'. Expected one of: " + validIds()
				));
		}

		public static String fromSystemPropertyId() {
			return System.getProperty(TEST_CASE_PROPERTY, "");
		}

		private static String validIds() {
			String singleTestCaseIds = Arrays.stream(values())
				.map(testCase -> testCase.id)
				.collect(Collectors.joining(", "));
			return "[" + ALL_TEST_CASES + ", " + singleTestCaseIds + "]";
		}
	}

	private static final class SingleplayerWorld implements AutoCloseable {
		private final String levelId;

		private SingleplayerWorld(String levelId) {
			this.levelId = levelId;
		}

		public static SingleplayerWorld create() {
			String levelId = "jei-client-test-" + UUID.randomUUID();
			ClientTestUtil.runOnClient(client -> {
				LevelSettings levelSettings = new LevelSettings(
					"JEI Client Test",
					GameType.CREATIVE,
					LevelSettings.DifficultySettings.DEFAULT,
					true,
					WorldDataConfiguration.DEFAULT
				);
				client.createWorldOpenFlows()
					.createFreshLevel(levelId, levelSettings, WorldOptions.testWorldWithRandomSeed(), WorldPresets::createTestWorldDimensions, new TitleScreen());
			}, WORLD_LOAD_TIMEOUT);
			ClientTestUtil.waitUntil(
				() -> ClientTestUtil.computeOnClient(client -> client.level != null && client.hasSingleplayerServer()),
				ASSERTION_TIMEOUT,
				() -> "Timed out creating integrated server world: " + levelId
			);
			return new SingleplayerWorld(levelId);
		}

		@Override
		public void close() {
			ClientTestUtil.runOnClient(client -> {
				if (client.level != null) {
					client.level.disconnect(Component.literal("Disconnecting"));
					client.disconnectWithSavingScreen();
				}
			}, WORLD_LOAD_TIMEOUT);
			ClientTestUtil.waitUntil(
				() -> ClientTestUtil.computeOnClient(client -> client.level == null && !client.hasSingleplayerServer()),
				ASSERTION_TIMEOUT,
				() -> "Timed out disconnecting from integrated server world: " + levelId
			);
			ClientTestUtil.runOnClient(client -> {
				try (LevelStorageSource.LevelStorageAccess access = client.getLevelSource().createAccess(levelId)) {
					access.deleteLevel();
				} catch (IOException e) {
					throw new AssertionError("Failed to delete integrated server test world: " + levelId, e);
				}
				client.gui.setScreen(new TitleScreen());
			}, WORLD_LOAD_TIMEOUT);
		}
	}
}
