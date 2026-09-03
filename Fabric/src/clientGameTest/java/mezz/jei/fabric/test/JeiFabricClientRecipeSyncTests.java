package mezz.jei.fabric.test;

import mezz.jei.common.Internal;
import mezz.jei.common.network.ClientConnectionHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

/**
 * Runs client-side recipe sync tests against disposable Fabric and vanilla servers.
 */
final class JeiFabricClientRecipeSyncTests {
	private static final String TEST_CASE_PROPERTY = "jei.clientRecipeSyncTest";
	private static final String JUNIT_SUITE_NAME = "fabric-client-recipe-sync";
	private static final Duration ASSERTION_TIMEOUT = Duration.ofSeconds(60);
	private static final ResourceLocation CRAFTING_TABLE_RECIPE_ID = ResourceLocation.withDefaultNamespace("crafting_table");

	private JeiFabricClientRecipeSyncTests() {

	}

	public static void register() {
		TestCase testCase = TestCase.fromSystemProperty();
		FabricClientTestRunner.register(getTestCase(testCase));
	}

	public static List<FabricClientTestRunner.ClientTestCase> getTestCases() {
		return Arrays.stream(TestCase.values())
			.map(JeiFabricClientRecipeSyncTests::getTestCase)
			.toList();
	}

	private static FabricClientTestRunner.ClientTestCase getTestCase(TestCase testCase) {
		return new FabricClientTestRunner.ClientTestCase(JUNIT_SUITE_NAME, testCase.id, testCase::run);
	}

	private static void runSingleplayerTestCase(String name, Runnable assertions) {
		try (FabricClientTestWorld ignored = FabricClientTestWorld.create()) {
			assertions.run();
		}
		assertClientRecipesCleared(name);
	}

	private static void runTestCase(String name, FabricExternalTestServer server, Runnable assertions) {
		try (FabricExternalTestServer.Connection ignored = server.connect()) {
			assertions.run();
		}
		assertClientRecipesCleared(name);
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
			() -> "Expected JEI to detect the integrated Fabric server with JEI, and use synced recipes. " + describeRecipeState()
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
			() -> "Expected JEI to detect a Fabric server with JEI, and use synced recipes. " + describeRecipeState()
		);
	}

	private static void assertSyncedRecipesFromFabricServerWithoutJei() {
		ClientTestUtil.waitUntil(
			() -> ClientTestUtil.computeOnClient(client -> hasJeiRuntime() &&
				Internal.hasClientSyncedRecipes() &&
				hasVanillaRecipes(Internal.getClientSyncedRecipes()) &&
				!Internal.getServerConnection().isJeiOnServer() &&
				Internal.getServerConnection().isSameModLoader()),
			ASSERTION_TIMEOUT,
			() -> "Expected JEI to detect a Fabric server without JEI and use synced vanilla recipes. " + describeRecipeState()
		);
	}

	private static void assertSyncedRecipesFromVanillaServer() {
		ClientTestUtil.waitUntil(
			() -> ClientTestUtil.computeOnClient(client -> hasJeiRuntime() &&
				Internal.hasClientSyncedRecipes() &&
				hasVanillaRecipes(Internal.getClientSyncedRecipes()) &&
				!Internal.getServerConnection().isJeiOnServer() &&
				!Internal.getServerConnection().isSameModLoader()),
			ASSERTION_TIMEOUT,
			() -> "Expected JEI to detect a vanilla server and use synced vanilla recipes. " + describeRecipeState()
		);
	}

	private static boolean hasVanillaRecipes(List<RecipeHolder<?>> recipes) {
		return !recipes.isEmpty() &&
			recipes.stream().anyMatch(recipe -> recipe.id().equals(CRAFTING_TABLE_RECIPE_ID));
	}

	private static void assertClientRecipesCleared(String name) {
		ClientTestUtil.waitUntil(
			() -> ClientTestUtil.computeOnClient(client ->
				client.level == null &&
					!hasJeiRuntime() &&
					!Internal.hasClientRecipes() &&
					Internal.getClientSyncedRecipes().isEmpty()),
			ASSERTION_TIMEOUT,
			() -> "Expected JEI to clear client recipes after disconnecting from " + name + ". " + describeRecipeState()
		);
	}

	private static String describeRecipeState() {
		return ClientTestUtil.computeOnClient(client -> {
			boolean hasRuntime = hasJeiRuntime();
			boolean hasSyncedRecipes = Internal.hasClientSyncedRecipes();
			List<RecipeHolder<?>> recipes = Internal.getClientSyncedRecipes();
			return "runtime=" + hasRuntime +
				", synced=" + hasSyncedRecipes +
				", recipeCount=" + recipes.size() +
				", hasCraftingTable=" + hasVanillaRecipes(recipes) +
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

	private enum TestCase {
		SINGLEPLAYER("singleplayer", "Fabric singleplayer") {
			@Override
			public void run() {
				runSingleplayerTestCase(displayName(), JeiFabricClientRecipeSyncTests::assertSyncedRecipesFromSingleplayer);
			}
		},
		FABRIC_SERVER_WITH_JEI("fabricServerWithJei", "Fabric server with JEI") {
			@Override
			public void run() {
				try (FabricExternalTestServer server = FabricExternalTestServer.startFabricWithJei()) {
					runTestCase(displayName(), server, JeiFabricClientRecipeSyncTests::assertSyncedRecipesFromJeiServer);
				}
			}
		},
		FABRIC_SERVER_WITHOUT_JEI("fabricServerWithoutJei", "Fabric server without JEI") {
			@Override
			public void run() {
				try (FabricExternalTestServer server = FabricExternalTestServer.startFabricWithoutJei()) {
					runTestCase(displayName(), server, JeiFabricClientRecipeSyncTests::assertSyncedRecipesFromFabricServerWithoutJei);
				}
			}
		},
		VANILLA_SERVER_WITHOUT_JEI("vanillaServerWithoutJei", "vanilla server without JEI") {
			@Override
			public void run() {
				try (FabricExternalTestServer server = FabricExternalTestServer.startVanilla()) {
					runTestCase(displayName(), server, JeiFabricClientRecipeSyncTests::assertSyncedRecipesFromVanillaServer);
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

		public static TestCase fromSystemProperty() {
			String id = System.getProperty(TEST_CASE_PROPERTY);
			return Arrays.stream(values())
				.filter(testCase -> testCase.id.equals(id))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException(
					"Unknown JEI Fabric client recipe sync test case '" + id + "'. Expected one of: " + validIds()
				));
		}

		private static String validIds() {
			return Arrays.stream(values())
				.map(testCase -> testCase.id)
				.toList()
				.toString();
		}
	}
}
