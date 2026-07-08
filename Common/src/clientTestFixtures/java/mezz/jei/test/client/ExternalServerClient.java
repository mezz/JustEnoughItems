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
import org.jspecify.annotations.Nullable;

import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicBoolean;
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
	private static final long SERVER_STATUS_PING_RETRY_INTERVAL_MILLIS = 1_000L;

	private ExternalServerClient() {

	}

	public static void assertNativeTransportDisabled(ClientAccess clientAccess) {
		boolean useNativeTransport = clientAccess.compute(client -> client.options.useNativeTransport);
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
				client.level.disconnect();
				client.disconnect();
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

	private static void waitForServerStatusPing(ExternalServerProcess server, ServerData serverData, ClientAccess clientAccess) {
		ServerStatusPinger pinger = new ServerStatusPinger();
		AtomicBoolean pingSucceeded = new AtomicBoolean(false);
		AtomicReference<UnknownHostException> pingStartFailure = new AtomicReference<>();
		AtomicLong nextPingAttemptMillis = new AtomicLong(0L);
		AtomicInteger pingAttempts = new AtomicInteger();

		try {
			clientAccess.waitFor(
				client -> {
					pinger.tick();
					if (pingSucceeded.get() || pingStartFailure.get() != null) {
						return true;
					}
					long now = System.currentTimeMillis();
					if (now >= nextPingAttemptMillis.get()) {
						startServerStatusPing(pinger, serverData, pingSucceeded, pingStartFailure, pingAttempts);
						nextPingAttemptMillis.set(now + SERVER_STATUS_PING_RETRY_INTERVAL_MILLIS);
					}
					return pingSucceeded.get() || pingStartFailure.get() != null;
				},
				() -> "Timed out waiting for external server status ping " + serverData.ip +
					" after " + pingAttempts.get() + " attempts (" +
					describeServerStatus(serverData) + ", " +
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
		assertServerStatusReady(serverData, pingAttempts.get());
	}

	private static void startServerStatusPing(
		ServerStatusPinger pinger,
		ServerData serverData,
		AtomicBoolean pingSucceeded,
		AtomicReference<UnknownHostException> pingStartFailure,
		AtomicInteger pingAttempts
	) {
		try {
			serverData.setState(ServerData.State.PINGING);
			pingAttempts.incrementAndGet();
			pinger.pingServer(
				serverData,
				() -> {},
				() -> {
					serverData.setState(
						serverData.protocol == SharedConstants.getProtocolVersion()
							? ServerData.State.SUCCESSFUL
							: ServerData.State.INCOMPATIBLE
					);
					pingSucceeded.set(true);
				}
			);
		} catch (UnknownHostException e) {
			serverData.setState(ServerData.State.UNREACHABLE);
			pingStartFailure.compareAndSet(null, e);
		}
	}

	private static void assertServerStatusReady(ServerData serverData, int pingAttempts) {
		if (serverData.state() != ServerData.State.SUCCESSFUL) {
			throw new AssertionError(
				"Expected external server status ping to succeed after " + pingAttempts + " attempts: " +
					describeServerStatus(serverData)
			);
		}
		if (serverData.ping < 0) {
			throw new AssertionError(
				"Expected external server status ping time to be set after " + pingAttempts + " attempts: " +
					describeServerStatus(serverData)
			);
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
		return "useNativeTransport=" + client.options.useNativeTransport +
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
