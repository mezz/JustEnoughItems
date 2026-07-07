package mezz.jei.fabric.test;

import mezz.jei.api.constants.ModIds;
import mezz.jei.test.lib.ExternalServerProcess;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.chat.Component;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Starts disposable vanilla or Fabric dedicated servers for client connection tests.
 */
@SuppressWarnings("UnstableApiUsage")
final class ExternalTestServer implements AutoCloseable {
	private final ClientGameTestContext context;
	private final ExternalServerProcess server;

	private ExternalTestServer(ClientGameTestContext context, ExternalServerProcess server) {
		this.context = context;
		this.server = server;
	}

	public static ExternalTestServer startVanilla(ClientGameTestContext context) {
		return start(context, "vanilla-server-without-jei", LaunchType.VANILLA);
	}

	public static ExternalTestServer startFabricWithJei(ClientGameTestContext context) {
		return start(context, "fabric-server-with-jei", LaunchType.FABRIC_WITH_JEI);
	}

	public static ExternalTestServer startFabricWithoutJei(ClientGameTestContext context) {
		return start(context, "fabric-server-without-jei", LaunchType.FABRIC_WITHOUT_JEI);
	}

	private static ExternalTestServer start(ClientGameTestContext context, String directoryName, LaunchType launchType) {
		ExternalServerProcess server = ExternalServerProcess.start(directoryName, launchType.description, launchType);
		return new ExternalTestServer(context, server);
	}

	public Connection connect() {
		context.runOnClient(client -> {
			String address = server.getConnectionAddress();
			ServerData serverData = new ServerData("JEI Test Server", address, ServerData.Type.OTHER);
			client.gui.hud.getChat().clearMessages(false);
			Screen screen = client.gui.screen();
			assert screen != null;
			ConnectScreen.startConnecting(screen, client, ServerAddress.parseString(address), serverData, false, null);
		});
		try {
			context.waitFor(client -> client.level != null, ClientGameTestContext.DEFAULT_TIMEOUT * 6);
		} catch (AssertionError e) {
			throw new AssertionError("Timed out connecting to external server " + server.getConnectionAddress() + ":\n" + server.readServerLogTail(), e);
		}
		return new Connection(context);
	}

	@Override
	public void close() {
		server.close();
	}

	private enum LaunchType implements ExternalServerProcess.Launcher {
		FABRIC_WITH_JEI("Fabric server with JEI", "net.fabricmc.loader.impl.launch.server.FabricServerLauncher") {
			@Override
			protected void addJvmArguments(List<String> command, int port) {
				command.add("-Dfabric.development=true");
			}

			@Override
			protected String classpath(Path serverDirectory) {
				return ExternalServerProcess.currentClasspath();
			}
		},
		FABRIC_WITHOUT_JEI("Fabric server without JEI", "net.fabricmc.loader.impl.launch.server.FabricServerLauncher") {
			@Override
			protected void addJvmArguments(List<String> command, int port) {
				command.add("-Dfabric.development=true");
			}

			@Override
			protected String classpath(Path serverDirectory) {
				return currentClasspathWithoutJei();
			}
		},
		VANILLA("vanilla server without JEI", "net.minecraft.server.Main") {
			@Override
			protected String classpath(Path serverDirectory) {
				return ExternalServerProcess.currentClasspath();
			}
		};

		private static final List<String> SERVER_WITHOUT_JEI_EXCLUDED_MOD_IDS = List.of(ModIds.JEI_ID, "jei-test");

		private final String description;
		private final String mainClass;

		LaunchType(String description, String mainClass) {
			this.description = description;
			this.mainClass = mainClass;
		}

		@Override
		public List<String> createCommand(Path serverDirectory, int port) {
			List<String> command = new ArrayList<>();
			command.add(ExternalServerProcess.javaBinary());
			command.add("-Xmx1G");
			addJvmArguments(command, port);
			command.add("-cp");
			command.add(classpath(serverDirectory));
			command.add(mainClass);
			command.add("nogui");
			return command;
		}

		protected void addJvmArguments(List<String> command, int port) {

		}

		protected abstract String classpath(Path serverDirectory);

		private static String currentClasspathWithoutJei() {
			Set<Path> excludedModRoots = getModRootPaths(SERVER_WITHOUT_JEI_EXCLUDED_MOD_IDS);
			return Arrays.stream(ExternalServerProcess.currentClasspath().split(File.pathSeparator))
				.filter(path -> !isExcludedModRoot(path, excludedModRoots))
				.collect(Collectors.joining(File.pathSeparator));
		}

		private static Set<Path> getModRootPaths(Collection<String> modIds) {
			FabricLoader loader = FabricLoader.getInstance();
			return modIds.stream()
				.map(loader::getModContainer)
				.flatMap(Optional::stream)
				.map(ModContainer::getRootPaths)
				.flatMap(Collection::stream)
				.map(LaunchType::normalizePath)
				.collect(Collectors.toUnmodifiableSet());
		}

		private static boolean isExcludedModRoot(String classpathEntry, Set<Path> excludedModRoots) {
			return excludedModRoots.contains(normalizePath(classpathEntry));
		}

		private static Path normalizePath(String path) {
			return normalizePath(Path.of(path));
		}

		private static Path normalizePath(Path path) {
			return path.toAbsolutePath().normalize();
		}
	}

	static final class Connection implements AutoCloseable {
		private final ClientGameTestContext context;

		private Connection(ClientGameTestContext context) {
			this.context = context;
		}

		@Override
		public void close() {
			context.runOnClient(client -> {
				if (client.level != null) {
					client.level.disconnect(Component.literal("Disconnecting"));
					client.disconnectWithSavingScreen();
				}
			});
			context.waitFor(client -> client.level == null);
			context.waitTicks(2);
			context.setScreen(TitleScreen::new);
		}
	}
}
