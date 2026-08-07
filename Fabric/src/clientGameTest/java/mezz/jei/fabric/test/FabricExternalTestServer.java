package mezz.jei.fabric.test;

import mezz.jei.api.constants.ModIds;
import mezz.jei.test.client.ExternalServerClient;
import mezz.jei.test.lib.ExternalServerProcess;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Starts disposable vanilla or Fabric dedicated servers for client recipe-sync tests.
 */
final class FabricExternalTestServer implements AutoCloseable {
	private static final Duration EXTERNAL_SERVER_CLIENT_TIMEOUT = Duration.ofSeconds(ExternalServerClient.EXTERNAL_SERVER_CLIENT_TIMEOUT_SECONDS);
	private static final ExternalServerClient.ClientAccess CLIENT_ACCESS = new ExternalServerClient.ClientAccess() {
		@Override
		public void run(Consumer<Minecraft> task) {
			ClientTestUtil.runOnClient(task::accept);
		}

		@Override
		public <T> T compute(Function<Minecraft, T> task) {
			return ClientTestUtil.computeOnClient(task::apply);
		}

		@Override
		public void waitFor(Predicate<Minecraft> predicate, Supplier<String> timeoutMessage) {
			ClientTestUtil.waitUntil(
				() -> ClientTestUtil.computeOnClient(predicate::test),
				EXTERNAL_SERVER_CLIENT_TIMEOUT,
				timeoutMessage
			);
		}

		@Override
		public void clearPlatformClientLevelReferences(Minecraft client) {
			clearFabricWorldRenderContext(client);
		}
	};

	private final ExternalServerProcess server;

	private FabricExternalTestServer(ExternalServerProcess server) {
		this.server = server;
	}

	private static void clearFabricWorldRenderContext(Minecraft client) {
		// Fabric API 0.116 retains the last ClientLevel here and does not expose a teardown method.
		for (Field field : client.levelRenderer.getClass().getDeclaredFields()) {
			if (field.getType().getName().equals("net.fabricmc.fabric.impl.client.rendering.WorldRenderContextImpl")) {
				try {
					field.setAccessible(true);
					Object context = field.get(client.levelRenderer);
					Field worldField = field.getType().getDeclaredField("world");
					worldField.setAccessible(true);
					worldField.set(context, null);
					return;
				} catch (ReflectiveOperationException e) {
					throw new AssertionError("Failed to clear Fabric's cached client level render context.", e);
				}
			}
		}
		throw new AssertionError("Failed to find Fabric's cached client level render context.");
	}

	public static FabricExternalTestServer startFabricWithJei() {
		return start("fabric-server-with-jei", LaunchType.FABRIC_WITH_JEI);
	}

	public static FabricExternalTestServer startFabricWithoutJei() {
		return start("fabric-server-without-jei", LaunchType.FABRIC_WITHOUT_JEI);
	}

	public static FabricExternalTestServer startVanilla() {
		return start("vanilla-server-without-jei", LaunchType.VANILLA);
	}

	private static FabricExternalTestServer start(String directoryName, LaunchType launchType) {
		ExternalServerClient.assertNativeTransportDisabled(CLIENT_ACCESS);
		ExternalServerProcess server = ExternalServerProcess.start(directoryName, launchType.description, launchType);
		return new FabricExternalTestServer(server);
	}

	public Connection connect() {
		ExternalServerClient.connect(server, CLIENT_ACCESS);
		return new Connection();
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

		private static final List<String> SERVER_WITHOUT_JEI_EXCLUDED_MOD_IDS = List.of(ModIds.JEI_ID, "jei-client-tests");

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
		private Connection() {

		}

		@Override
		public void close() {
			ExternalServerClient.disconnect(CLIENT_ACCESS);
		}
	}
}
