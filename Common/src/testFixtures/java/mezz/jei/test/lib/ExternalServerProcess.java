package mezz.jei.test.lib;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Starts and owns a disposable dedicated server process for client tests.
 */
public final class ExternalServerProcess implements AutoCloseable {
	private static final Duration SERVER_START_TIMEOUT = Duration.ofSeconds(90);
	private static final Duration SERVER_STOP_TIMEOUT = Duration.ofSeconds(20);

	private final TemporaryDirectory temporaryDirectory;
	private final Process process;
	// Keep the child stdin pipe open so dedicated servers do not treat EOF as an immediate shutdown signal.
	private final BufferedWriter processInput;
	private final Path serverDirectory;
	private final Path serverLog;
	private final int port;

	private ExternalServerProcess(TemporaryDirectory temporaryDirectory, Process process, BufferedWriter processInput, Path serverDirectory, Path serverLog, int port) {
		this.temporaryDirectory = temporaryDirectory;
		this.process = process;
		this.processInput = processInput;
		this.serverDirectory = serverDirectory;
		this.serverLog = serverLog;
		this.port = port;
	}

	public static ExternalServerProcess start(String directoryName, String description, Launcher launcher) {
		TemporaryDirectory temporaryDirectory = TemporaryDirectory.create(directoryName);
		Path serverDirectory = temporaryDirectory.path().resolve("server");
		Path serverLog = serverDirectory.resolve("server.log");
		int port = findAvailablePort();

		try {
			Files.createDirectories(serverDirectory);
			writeServerFiles(serverDirectory, port);
		} catch (IOException e) {
			closeOnStartFailure(temporaryDirectory, e);
			throw new AssertionError("Failed to prepare " + description + " in " + serverDirectory, e);
		}

		List<String> command;
		try {
			command = launcher.createCommand(serverDirectory, port);
		} catch (RuntimeException | Error e) {
			closeOnStartFailure(temporaryDirectory, e);
			throw e;
		}

		ProcessBuilder processBuilder = new ProcessBuilder(command)
			.directory(serverDirectory.toFile())
			.redirectErrorStream(true)
			.redirectOutput(serverLog.toFile());
		launcher.configureEnvironment(processBuilder.environment());

		Process process;
		try {
			process = processBuilder.start();
		} catch (IOException e) {
			closeOnStartFailure(temporaryDirectory, e);
			throw new AssertionError("Failed to start " + description + " in " + serverDirectory, e);
		} catch (RuntimeException | Error e) {
			closeOnStartFailure(temporaryDirectory, e);
			throw e;
		}

		BufferedWriter processInput = process.outputWriter(StandardCharsets.UTF_8);
		ExternalServerProcess server = new ExternalServerProcess(temporaryDirectory, process, processInput, serverDirectory, serverLog, port);
		try {
			server.waitUntilReady();
			return server;
		} catch (RuntimeException | Error e) {
			closeOnStartFailure(server, e);
			throw e;
		}
	}

	public String getConnectionAddress() {
		return "127.0.0.1:" + port;
	}

	public String describeProcessState() {
		if (process.isAlive()) {
			return "external server process is still running";
		}
		return "external server process exited with code " + process.exitValue();
	}

	public String readServerLogTail() {
		if (!Files.exists(serverLog)) {
			return "Server log does not exist: " + serverLog;
		}

		try {
			List<String> lines = Files.readAllLines(serverLog);
			return lines.stream()
				.skip(Math.max(0, lines.size() - 80))
				.collect(Collectors.joining(System.lineSeparator()));
		} catch (IOException e) {
			return "Failed to read server log " + serverLog + ": " + e.getMessage();
		}
	}

	@Override
	public void close() {
		try {
			stopServerProcess(process, processInput);
		} finally {
			temporaryDirectory.close();
		}
	}

	public static String currentClasspath() {
		return System.getProperty("java.class.path");
	}

	public static String javaBinary() {
		String executable = "java";
		if (System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("win")) {
			executable = "java.exe";
		}
		return Path.of(System.getProperty("java.home"), "bin", executable).toString();
	}

	private void waitUntilReady() {
		Instant deadline = Instant.now().plus(SERVER_START_TIMEOUT);

		while (Instant.now().isBefore(deadline)) {
			if (!process.isAlive()) {
				throw new AssertionError("External server stopped during startup:\n" + readServerLogTail());
			}
			if (isServerDone()) {
				return;
			}
			sleepDuringServerStartup();
		}

		throw new AssertionError("Timed out starting external server in " + serverDirectory + ":\n" + readServerLogTail());
	}

	private boolean isServerDone() {
		try {
			return Files.exists(serverLog) && Files.readString(serverLog).contains("Done (");
		} catch (IOException e) {
			throw new AssertionError("Failed to read external server log: " + serverLog, e);
		}
	}

	private static void sleepDuringServerStartup() {
		try {
			Thread.sleep(100L);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new AssertionError("Interrupted while waiting for external server startup", e);
		}
	}

	private static void stopServerProcess(Process process, BufferedWriter processInput) {
		try {
			if (process.isAlive()) {
				try {
					processInput.write("stop");
					processInput.newLine();
					processInput.flush();
				} catch (IOException ignored) {

				}

				if (!process.waitFor(SERVER_STOP_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS)) {
					process.destroy();
					if (!process.waitFor(SERVER_STOP_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS)) {
						process.destroyForcibly();
					}
				}
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			process.destroyForcibly();
			throw new AssertionError("Interrupted while stopping external server", e);
		} finally {
			try {
				processInput.close();
			} catch (IOException ignored) {

			}
		}
	}

	private static void closeOnStartFailure(ExternalServerProcess server, Throwable failure) {
		try {
			server.close();
		} catch (Throwable cleanupFailure) {
			failure.addSuppressed(cleanupFailure);
		}
	}

	private static void closeOnStartFailure(TemporaryDirectory temporaryDirectory, Throwable failure) {
		try {
			temporaryDirectory.close();
		} catch (Throwable cleanupFailure) {
			failure.addSuppressed(cleanupFailure);
		}
	}

	private static int findAvailablePort() {
		try (ServerSocket socket = new ServerSocket(0)) {
			socket.setReuseAddress(true);
			return socket.getLocalPort();
		} catch (IOException e) {
			throw new AssertionError("Failed to find an available server port", e);
		}
	}

	private static void writeServerFiles(Path serverDirectory, int port) throws IOException {
		Files.writeString(serverDirectory.resolve("eula.txt"), "eula=true\n", StandardCharsets.UTF_8);

		try (BufferedWriter writer = Files.newBufferedWriter(serverDirectory.resolve("server.properties"), StandardCharsets.UTF_8)) {
			MinimalWorldGenServerProperties.createForLocalhost(port)
				.store(writer, "JEI client game test server");
		}
	}

	public interface Launcher {
		List<String> createCommand(Path serverDirectory, int port);

		default void configureEnvironment(Map<String, String> environment) {

		}
	}

}
