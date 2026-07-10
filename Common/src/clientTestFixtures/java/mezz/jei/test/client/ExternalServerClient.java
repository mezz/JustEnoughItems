package mezz.jei.test.client;

import mezz.jei.test.lib.ExternalServerProcess;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerStatusPinger;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.chat.Component;
import net.minecraft.server.network.EventLoopGroupHolder;
import org.jspecify.annotations.Nullable;

import java.net.UnknownHostException;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Coordinates Minecraft client connections to disposable external test servers.
 */
public final class ExternalServerClient {
	public static final int EXTERNAL_SERVER_CLIENT_TIMEOUT_SECONDS = 60;
	public static final int EXTERNAL_SERVER_CLIENT_TIMEOUT_TICKS = EXTERNAL_SERVER_CLIENT_TIMEOUT_SECONDS * SharedConstants.TICKS_PER_SECOND;
	private static final Duration SERVER_STATUS_PING_RETRY_INTERVAL = Duration.ofMillis(50L);

	private ExternalServerClient() {

	}

	public static void assertNativeTransportDisabled(ClientAccess clientAccess) {
		boolean useNativeTransport = clientAccess.compute(client -> client.options.useNativeTransport());
		if (useNativeTransport) {
			throw new AssertionError("Expected client options to disable native transport before connecting to the external test server.");
		}
	}

	public static void connect(ExternalServerProcess server, ClientAccess clientAccess) {
		ServerData serverData = createServerData(server);
		assertClientReadyToConnect(clientAccess);
		waitForServerStatusPing(server, serverData, clientAccess);
		assertClientReadyToConnect(clientAccess);
		clientAccess.run(client -> {
			client.gui.getChat().clearMessages(false);
			@Nullable Screen screen = client.screen;
			if (screen == null) {
				throw new AssertionError("Expected a parent screen before connecting to the external test server.");
			}
			ConnectScreen.startConnecting(screen, client, ServerAddress.parseString(serverData.ip), serverData, false, null);
		});
		clientAccess.waitFor(
			ExternalServerClient::isClientConnected,
			() -> "Timed out connecting to external server " + server.getConnectionAddress() + " (" +
				describeClientState(clientAccess) + ", " +
				server.describeProcessState() + "):\n" +
				server.readServerLogTail()
		);
	}

	public static void disconnect(ClientAccess clientAccess) {
		clientAccess.run(client -> {
			if (client.level != null) {
				client.level.disconnect(Component.literal("Disconnecting"));
				client.disconnectWithSavingScreen();
			}
		});
		clientAccess.waitFor(
			ExternalServerClient::isClientDisconnected,
			() -> "Timed out disconnecting from external server. " + describeClientState(clientAccess)
		);
		clientAccess.run(client -> client.setScreen(new TitleScreen()));
		clientAccess.waitFor(
			client -> client.screen instanceof TitleScreen && isClientDisconnected(client),
			() -> "Timed out returning to the title screen after disconnecting from external server. " + describeClientState(clientAccess)
		);
	}

	private static ServerData createServerData(ExternalServerProcess server) {
		return new ServerData("JEI Test Server", server.getConnectionAddress(), ServerData.Type.OTHER);
	}

	private static ServerData copyServerData(ServerData serverData) {
		return new ServerData(serverData.name, serverData.ip, serverData.type());
	}

	private static void waitForServerStatusPing(ExternalServerProcess server, ServerData serverData, ClientAccess clientAccess) {
		ServerStatusPinger pinger = new ServerStatusPinger();
		AtomicReference<ServerData> latestPingData = new AtomicReference<>(serverData);
		AtomicReference<ServerData> successfulPingData = new AtomicReference<>();
		AtomicReference<UnknownHostException> pingStartFailure = new AtomicReference<>();
		AtomicLong nextPingAttemptMillis = new AtomicLong(0L);
		AtomicInteger pingAttempts = new AtomicInteger();

		try {
			clientAccess.waitFor(
				client -> {
					pinger.tick();
					if (pingStartFailure.get() != null) {
						return true;
					}
					long now = System.currentTimeMillis();
					if (now >= nextPingAttemptMillis.get()) {
						startServerStatusPing(
							client,
							pinger,
							serverData,
							latestPingData,
							successfulPingData,
							pingStartFailure,
							pingAttempts
						);
						nextPingAttemptMillis.set(now + SERVER_STATUS_PING_RETRY_INTERVAL.toMillis());
					}
					return successfulPingData.get() != null || pingStartFailure.get() != null;
				},
				() -> "Timed out waiting for external server status ping " + serverData.ip +
					" after " + pingAttempts.get() + " attempts (" +
					describeServerStatus(latestPingData.get()) + ", " +
					server.describeProcessState() + "):\n" +
					server.readServerLogTail()
			);
		} catch (AssertionError e) {
			UnknownHostException startFailure = pingStartFailure.get();
			if (startFailure != null) {
				throw new AssertionError("Failed to resolve external server " + serverData.ip, startFailure);
			}
			throw e;
		} finally {
			clientAccess.run(client -> pinger.removeAll());
		}

		UnknownHostException startFailure = pingStartFailure.get();
		if (startFailure != null) {
			throw new AssertionError("Failed to resolve external server " + serverData.ip, startFailure);
		}
		if (successfulPingData.get() == null) {
			throw new AssertionError(
				"Expected external server status ping to succeed after " + pingAttempts.get() + " attempts: " +
					describeServerStatus(latestPingData.get())
			);
		}
	}

	private static void startServerStatusPing(
		Minecraft client,
		ServerStatusPinger pinger,
		ServerData serverData,
		AtomicReference<ServerData> latestPingData,
		AtomicReference<ServerData> successfulPingData,
		AtomicReference<UnknownHostException> pingStartFailure,
		AtomicInteger pingAttempts
	) {
		ServerData pingData = copyServerData(serverData);
		pingData.setState(ServerData.State.PINGING);
		latestPingData.set(pingData);
		try {
			pingAttempts.incrementAndGet();
			pinger.pingServer(
				pingData,
				() -> {},
				() -> {
					if (pingData.protocol == SharedConstants.getCurrentVersion().protocolVersion()) {
						pingData.setState(ServerData.State.SUCCESSFUL);
						successfulPingData.compareAndSet(null, pingData);
					} else {
						pingData.setState(ServerData.State.INCOMPATIBLE);
					}
					latestPingData.set(pingData);
				},
				EventLoopGroupHolder.remote(client.options.useNativeTransport())
			);
		} catch (UnknownHostException e) {
			pingData.setState(ServerData.State.UNREACHABLE);
			latestPingData.set(pingData);
			pingStartFailure.compareAndSet(null, e);
		}
	}

	private static void assertClientReadyToConnect(ClientAccess clientAccess) {
		@Nullable String notReadyReason = clientAccess.compute(ExternalServerClient::getClientNotReadyReason);
		if (notReadyReason != null) {
			throw new AssertionError("Expected client to be ready before connecting to the external test server: " + notReadyReason);
		}
	}

	private static @Nullable String getClientNotReadyReason(Minecraft client) {
		if (!isClientDisconnected(client)) {
			return "client is still connected: " + describeClientState(client);
		}
		@Nullable Screen screen = client.screen;
		if (screen == null) {
			return "client has no parent screen: " + describeClientState(client);
		}
		if (screen instanceof ConnectScreen) {
			return "client is already connecting: " + describeClientState(client);
		}
		return null;
	}

	private static boolean isClientConnected(Minecraft client) {
		return client.level != null &&
			client.player != null &&
			client.getConnection() != null;
	}

	private static boolean isClientDisconnected(Minecraft client) {
		return client.level == null &&
			client.player == null &&
			client.getConnection() == null;
	}

	private static String describeClientState(ClientAccess clientAccess) {
		return clientAccess.compute(ExternalServerClient::describeClientState);
	}

	private static String describeClientState(Minecraft client) {
		@Nullable Screen screen = client.screen;
		return "useNativeTransport=" + client.options.useNativeTransport() +
			", hasLevel=" + (client.level != null) +
			", hasPlayer=" + (client.player != null) +
			", hasConnection=" + (client.getConnection() != null) +
			", screen=" + (screen == null ? "null" : screen.getClass().getName());
	}

	private static String describeServerStatus(ServerData serverData) {
		return "state=" + serverData.state() +
			", motd=" + describeComponent(serverData.motd) +
			", status=" + describeComponent(serverData.status) +
			", ping=" + serverData.ping +
			", protocol=" + serverData.protocol +
			", version=" + describeComponent(serverData.version);
	}

	private static String describeComponent(@Nullable Component component) {
		if (component == null) {
			return "null";
		}
		return component.getString();
	}

	public interface ClientAccess {
		void run(Consumer<Minecraft> task);

		<T> T compute(Function<Minecraft, T> task);

		void waitFor(Predicate<Minecraft> predicate, Supplier<String> timeoutMessage);
	}
}
