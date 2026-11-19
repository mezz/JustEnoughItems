package mezz.jei.forge.test;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.common.Internal;
import mezz.jei.test.client.ExternalServerClient;
import mezz.jei.test.client.FakeMenuClientTest;
import mezz.jei.test.lib.ExternalServerProcess;
import mezz.jei.test.lib.JUnitXmlTestReporter;
import mezz.jei.test.lib.SpriteRenderingTest;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Exercises JEI's Forge startup through real client logins, tag packets, and plugin registration.
 */
@Mod("jei_client_tests")
public final class JeiForgeClientTests {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final ExternalServerClient.ClientAccess CLIENT_ACCESS = new ExternalServerClient.ClientAccess() {
		@Override
		public void run(Consumer<Minecraft> task) {
			ForgeClientTestUtil.run(task);
		}

		@Override
		public <T> T compute(Function<Minecraft, T> task) {
			return ForgeClientTestUtil.compute(task);
		}

		@Override
		public void waitFor(Predicate<Minecraft> predicate, Supplier<String> timeoutMessage) {
			ForgeClientTestUtil.waitUntil(() -> ForgeClientTestUtil.compute(predicate::test), timeoutMessage);
		}
	};

	private final AtomicBoolean started = new AtomicBoolean();

	public JeiForgeClientTests() {
		MinecraftForge.EVENT_BUS.addListener(this::onClientTick);
	}

	private void onClientTick(TickEvent.ClientTickEvent event) {
		if (event.phase == TickEvent.Phase.END && started.compareAndSet(false, true)) {
			new Thread(this::runTests, "JEI Forge Client GameTests").start();
		}
	}

	private void runTests() {
		int exitCode = 0;
		try {
			ForgeClientTestUtil.waitUntil(
				() -> ForgeClientTestUtil.compute(client -> client.getOverlay() == null && client.screen != null),
				"Timed out waiting for the Forge client startup screen"
			);
			String suite = System.getProperty("jei.forge.clientTest", "all");
			List<String> tests;
			if (suite.equals("all")) {
				tests = List.of("spriteRendering", "dedicated", "clientOnly", "singleplayer");
			} else {
				tests = List.of(suite);
			}
			for (String test : tests) {
				LOGGER.info("Running JEI Forge client GameTest: {}", test);
				JUnitXmlTestReporter.runAndReport("forge-client-startup", test, () -> {
					switch (test) {
						case "spriteRendering" -> ForgeClientTestUtil.run(SpriteRenderingTest::run);
						case "dedicated" -> testDedicated(true);
						case "clientOnly" -> testDedicated(false);
						case "singleplayer" -> testSingleplayer();
						default -> throw new IllegalArgumentException("Unknown Forge client GameTest: " + test);
					}
				});
				LOGGER.info("JEI Forge client GameTest passed: {}", test);
			}
		} catch (Throwable error) {
			exitCode = 1;
			LOGGER.error("JEI Forge client GameTest failed", error);
		} finally {
			try {
				ForgeClientTestUtil.run(Minecraft::stop);
			} catch (Throwable error) {
				exitCode = 1;
				LOGGER.error("Failed to stop the Forge client after GameTests", error);
			}
			if (exitCode != 0) {
				System.exit(exitCode);
			}
		}
	}

	private static void testDedicated(boolean jeiOnServer) {
		ExternalServerClient.assertNativeTransportDisabled(CLIENT_ACCESS);
		ForgeClientTestUtil.run(client -> StartupRecipePlugin.reset());
		try (ExternalServerProcess server = ExternalServerProcess.start(
			"forge-startup", "Forge startup test server", new ServerLauncher(jeiOnServer)
		)) {
			try {
				ExternalServerClient.connect(server, CLIENT_ACCESS);
				assertRegistration(1, jeiOnServer);
				server.sendCommand("reload");
				assertRegistration(2, jeiOnServer);
				disconnect();
				ExternalServerClient.connect(server, CLIENT_ACCESS);
				assertRegistration(3, jeiOnServer);
			} finally {
				disconnect();
			}
		}
	}

	private static void disconnect() {
		ForgeClientTestUtil.run(client -> {
			if (client.level != null) {
				client.level.disconnect();
				client.clearLevel();
			}
			client.setScreen(new TitleScreen());
		});
		ForgeClientTestUtil.waitUntil(
			() -> ForgeClientTestUtil.compute(client -> client.level == null && client.getConnection() == null && !client.hasSingleplayerServer()),
			"Timed out disconnecting the Forge test client"
		);
	}

	private static void assertRegistration(int expectedCount, boolean jeiOnServer) {
		ForgeClientTestUtil.waitUntil(
			() -> ForgeClientTestUtil.compute(client -> Internal.getOptionalJeiRuntime().isPresent() && StartupRecipePlugin.registrationCount() >= expectedCount),
			"Timed out waiting for JEI recipe registration " + expectedCount
		);
		ForgeClientTestUtil.run(client -> {
			StartupRecipePlugin.assertTagsResolved();
			if (StartupRecipePlugin.registrationCount() != expectedCount) {
				throw new AssertionError("Expected one JEI registration per login or reload, got " + StartupRecipePlugin.registrationCount());
			}
			if (!Internal.hasClientSyncedRecipes() || Internal.getServerConnection().isJeiOnServer() != jeiOnServer) {
				throw new AssertionError("JEI detected the wrong recipe sync or server mod state");
			}
			boolean recipeRegistered = Internal.getJeiRuntime().getRecipeManager()
				.createRecipeLookup(RecipeTypes.CRAFTING).get()
				.anyMatch(recipe -> recipe.getId().equals(StartupRecipePlugin.RECIPE_ID));
			if (!recipeRegistered) {
				throw new AssertionError("The plugin's tagged recipe is missing from JEI");
			}
		});
	}

	private static void testSingleplayer() {
		String levelId = "jei-forge-startup-" + UUID.randomUUID();
		try {
			ForgeClientTestUtil.run(client -> {
				StartupRecipePlugin.reset();
				client.createWorldOpenFlows().createFreshLevel(levelId,
					new LevelSettings("JEI Startup GameTest", GameType.CREATIVE, false, Difficulty.PEACEFUL, true,
						new GameRules(), WorldDataConfiguration.DEFAULT),
					new WorldOptions(0L, false, false),
					access -> access.registryOrThrow(Registries.WORLD_PRESET).getOrThrow(WorldPresets.FLAT).createWorldDimensions());
			});
			assertRegistration(1, true);
			FakeMenuClientTest.run(ForgeClientTestUtil::run, condition -> ForgeClientTestUtil.waitUntil(
				() -> ForgeClientTestUtil.compute(condition::test), "Timed out waiting for fake-menu player replacement"
			));
		} finally {
			disconnect();
			ForgeClientTestUtil.run(client -> {
				try (var access = client.getLevelSource().createAccess(levelId)) {
					access.deleteLevel();
				} catch (IOException e) {
					throw new AssertionError("Failed to delete the integrated startup test world", e);
				}
			});
		}
	}

	private record ServerLauncher(boolean jeiOnServer) implements ExternalServerProcess.Launcher {
		private static Properties readLaunchSettings() {
			Properties launch = new Properties();
			try (var reader = Files.newBufferedReader(Path.of(System.getProperty("jei.test.serverLaunch")))) {
				launch.load(reader);
			} catch (IOException e) {
				throw new AssertionError("Failed to read the Forge server launch settings", e);
			}
			return launch;
		}

		@Override
		public List<String> createCommand(Path serverDirectory, int port) {
			Properties launch = readLaunchSettings();
			List<String> command = new ArrayList<>();
			command.add(launch.getProperty("java"));
			command.add("-Xmx1G");
			Arrays.stream(launch.getProperty("arguments").split("\n"))
				.filter(argument -> jeiOnServer || !argument.startsWith("-Dfml.modFolders="))
				.forEach(command::add);
			return command;
		}

		@Override
		public void configureEnvironment(Map<String, String> environment) {
			// Use the launch file's mod folders so the child cannot inherit the client's test mod.
			environment.remove("MOD_CLASSES");
			if (jeiOnServer) {
				environment.put("MOD_CLASSES", readLaunchSettings().getProperty("modClasses"));
			}
		}
	}
}
