package mezz.jei.neoforge.tests.client;

import mezz.jei.test.lib.ExternalServerProcess;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Starts disposable vanilla or NeoForge dedicated servers for client recipe-sync tests.
 */
final class NeoForgeExternalTestServer implements AutoCloseable {
	private static final Duration CLIENT_CONNECT_TIMEOUT = Duration.ofSeconds(60);

	private final ExternalServerProcess server;

	private NeoForgeExternalTestServer(ExternalServerProcess server) {
		this.server = server;
	}

	public static NeoForgeExternalTestServer startNeoForgeWithJei() {
		return start("neoforge-server-with-jei", LaunchType.NEOFORGE_WITH_JEI);
	}

	public static NeoForgeExternalTestServer startNeoForgeWithoutJei() {
		return start("neoforge-server-without-jei", LaunchType.NEOFORGE_WITHOUT_JEI);
	}

	public static NeoForgeExternalTestServer startVanilla() {
		return start("vanilla-server-without-jei", LaunchType.VANILLA);
	}

	private static NeoForgeExternalTestServer start(String directoryName, LaunchType launchType) {
		ExternalServerProcess server = ExternalServerProcess.start(directoryName, launchType.description, launchType);
		return new NeoForgeExternalTestServer(server);
	}

	public Connection connect() {
		ClientTestUtil.runOnClient(client -> {
			String address = server.getConnectionAddress();
			ServerData serverData = new ServerData("JEI Test Server", address, ServerData.Type.OTHER);
			client.gui.getChat().clearMessages(false);
			Screen screen = client.screen;
			assert screen != null;
			ConnectScreen.startConnecting(screen, client, ServerAddress.parseString(address), serverData, false, null);
		});
		ClientTestUtil.waitUntil(
			() -> ClientTestUtil.computeOnClient(client -> client.level != null),
			CLIENT_CONNECT_TIMEOUT,
			() -> "Timed out connecting to external server " + server.getConnectionAddress() + ":\n" + server.readServerLogTail()
		);
		return new Connection();
	}

	@Override
	public void close() {
		server.close();
	}

	private enum LaunchType implements ExternalServerProcess.Launcher {
		NEOFORGE_WITH_JEI("NeoForge server with JEI") {
			@Override
			public List<String> createCommand(Path serverDirectory, int port) {
				return createDevLaunchCommand(ExternalServerLaunchConfig.get().neoForgeServerWithJei());
			}
		},
		NEOFORGE_WITHOUT_JEI("NeoForge server without JEI") {
			@Override
			public List<String> createCommand(Path serverDirectory, int port) {
				return createDevLaunchCommand(ExternalServerLaunchConfig.get().neoForgeServerWithoutJei());
			}
		},
		VANILLA("vanilla server without JEI") {
			@Override
			public List<String> createCommand(Path serverDirectory, int port) {
				return createDevLaunchCommand(ExternalServerLaunchConfig.get().vanillaServer());
			}
		};

		private static final String DEV_LAUNCH_MAIN_CLASS = "net.neoforged.devlaunch.Main";

		private final String description;

		LaunchType(String description) {
			this.description = description;
		}

		private static List<String> createDevLaunchCommand(DevLaunchServerConfig launchConfig) {
			List<String> command = new ArrayList<>();
			command.add(ExternalServerProcess.javaBinary());
			command.add("-Xmx1G");
			command.add("@" + launchConfig.classpathArgsFile());
			command.add("@" + launchConfig.vmArgsFile());
			command.add("-Dfml.modFolders=" + launchConfig.modFolders());
			command.add(DEV_LAUNCH_MAIN_CLASS);
			command.add("@" + launchConfig.programArgsFile());
			return command;
		}
	}

	private record DevLaunchServerConfig(
		String classpathArgsFile,
		String vmArgsFile,
		String programArgsFile,
		String modFolders
	) {

	}

	private static final class ExternalServerLaunchConfig {
		private static final String RESOURCE_NAME = "/jei-external-server-launch.properties";
		private static final ExternalServerLaunchConfig INSTANCE = load();

		private final DevLaunchServerConfig neoForgeServerWithJei;
		private final DevLaunchServerConfig neoForgeServerWithoutJei;
		private final DevLaunchServerConfig vanillaServer;

		private ExternalServerLaunchConfig(
			DevLaunchServerConfig neoForgeServerWithJei,
			DevLaunchServerConfig neoForgeServerWithoutJei,
			DevLaunchServerConfig vanillaServer
		) {
			this.neoForgeServerWithJei = neoForgeServerWithJei;
			this.neoForgeServerWithoutJei = neoForgeServerWithoutJei;
			this.vanillaServer = vanillaServer;
		}

		public static ExternalServerLaunchConfig get() {
			return INSTANCE;
		}

		public DevLaunchServerConfig neoForgeServerWithJei() {
			return neoForgeServerWithJei;
		}

		public DevLaunchServerConfig neoForgeServerWithoutJei() {
			return neoForgeServerWithoutJei;
		}

		public DevLaunchServerConfig vanillaServer() {
			return vanillaServer;
		}

		private static ExternalServerLaunchConfig load() {
			Properties properties = new Properties();
			try (InputStream input = NeoForgeExternalTestServer.class.getResourceAsStream(RESOURCE_NAME)) {
				if (input == null) {
					throw new AssertionError("Could not find external server launch config resource: " + RESOURCE_NAME);
				}
				properties.load(input);
			} catch (IOException e) {
				throw new AssertionError("Failed to read external server launch config resource: " + RESOURCE_NAME, e);
			}

			return new ExternalServerLaunchConfig(
				loadDevLaunchServerConfig(properties, "neoForgeServerWithJei"),
				loadDevLaunchServerConfig(properties, "neoForgeServerWithoutJei"),
				loadDevLaunchServerConfig(properties, "vanillaServer")
			);
		}

		private static DevLaunchServerConfig loadDevLaunchServerConfig(Properties properties, String keyPrefix) {
			return new DevLaunchServerConfig(
				requiredFile(properties, keyPrefix + ".classpathArgsFile"),
				requiredFile(properties, keyPrefix + ".vmArgsFile"),
				requiredFile(properties, keyPrefix + ".programArgsFile"),
				required(properties, keyPrefix + ".modFolders")
			);
		}

		private static String requiredFile(Properties properties, String key) {
			String value = required(properties, key);
			if (!Files.exists(Path.of(value))) {
				throw new AssertionError("External server launch config file does not exist for " + key + ": " + value);
			}
			return value;
		}

		private static String required(Properties properties, String key) {
			String value = properties.getProperty(key);
			if (value == null) {
				throw new AssertionError("External server launch config is missing required property: " + key);
			}
			return value;
		}
	}

	static final class Connection implements AutoCloseable {
		private Connection() {

		}

		@Override
		public void close() {
			ClientTestUtil.runOnClient(client -> {
				if (client.level != null) {
					client.level.disconnect();
					client.disconnect();
				}
			});
			ClientTestUtil.waitUntil(
				() -> ClientTestUtil.computeOnClient(client -> client.level == null),
				CLIENT_CONNECT_TIMEOUT,
				() -> "Timed out disconnecting from external server."
			);
			ClientTestUtil.runOnClient(client -> client.setScreen(new TitleScreen()));
		}
	}
}
