package mezz.jei.neoforge.startup;

import mezz.jei.common.Internal;
import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.neoforge.events.PermanentEventSubscriptions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.Connection;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RecipesUpdatedEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;

/**
 * This class observes events and determines when it's the right time to start JEI.
 *
 * JEI needs to see {@link ClientPlayerNetworkEvent.LoggingIn} before it is ready to start. When
 * the connection can provide server recipe content, JEI also waits for {@link RecipesUpdatedEvent}
 * so it does not briefly start with fallback client recipes.
 *
 * Connections that never provide synced recipes continue with fallback recipes.
 * Datapack reloads can fire another recipe event after JEI has started; if that event provides
 * synced recipes, JEI restarts using the synced recipes.
 */
public class StartEventObserver implements ResourceManagerReloadListener {
	private static final Logger LOGGER = LogManager.getLogger();

	private enum State {
		LISTENING, JEI_STARTED
	}

	private final IConnectionToServer serverConnection;
	private final Runnable startRunnable;
	private final Runnable stopRunnable;
	private WeakReference<Connection> currentConnection = new WeakReference<>(null);
	private State state = State.LISTENING;
	private boolean observedLogin;
	private boolean observedRecipeSync;

	public StartEventObserver(IConnectionToServer serverConnection, Runnable startRunnable, Runnable stopRunnable) {
		this.serverConnection = serverConnection;
		this.startRunnable = startRunnable;
		this.stopRunnable = stopRunnable;
	}

	public void register(PermanentEventSubscriptions subscriptions) {
		subscriptions.register(EventPriority.LOWEST, ClientPlayerNetworkEvent.LoggingIn.class, this::onLoggingIn);
		subscriptions.register(EventPriority.LOWEST, RecipesUpdatedEvent.class, this::onRecipesUpdatedEvent);

		subscriptions.register(ClientPlayerNetworkEvent.LoggingOut.class, event -> {
			if (event.getPlayer() != null) {
				logReceivedEvent(event);
				Internal.clearClientRecipes();
				transitionState(State.LISTENING);
			}
		});

		subscriptions.register(ScreenEvent.Init.Pre.class, event -> {
			if (this.state != State.JEI_STARTED) {
				Screen screen = event.getScreen();
				Minecraft minecraft = screen.getMinecraft();
				if (screen instanceof AbstractContainerScreen && minecraft != null && minecraft.player != null) {
					LOGGER.error("""
							A Screen is opening but JEI hasn't started yet.
							Normally, JEI is started after these events have fired: {}.
							Something has caused one or more of these events to fail, so JEI is starting very late.
							Missing events: {}""",
						getRequiredStartEventsString(),
						getMissingStartEventsString()
					);
					transitionState(State.LISTENING);
					transitionState(State.JEI_STARTED);
				}
			}
		});
	}

	private void onLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
		if (!observeConnectionEvent(event)) {
			return;
		}
		this.observedLogin = true;
		startIfReady();
	}

	private void onRecipesUpdatedEvent(RecipesUpdatedEvent event) {
		if (!observeConnectionEvent(event)) {
			return;
		}
		this.observedRecipeSync = true;
		if (this.state == State.JEI_STARTED && Internal.hasClientSyncedRecipes()) {
			restart();
		} else {
			startIfReady();
		}
	}

	private void startIfReady() {
		if (this.state != State.LISTENING || !this.observedLogin) {
			return;
		}
		if (shouldWaitForRecipes() && !this.observedRecipeSync) {
			return;
		}
		transitionState(State.JEI_STARTED);
	}

	private <T extends Event> boolean observeConnectionEvent(T event) {
		Connection observingConnection = this.currentConnection.get();
		Connection currentConnection = getCurrentConnection();
		if (currentConnection != observingConnection) {
			// Connection changed => any information we previously got is useless now
			clearObservedStartEvents();
			this.currentConnection = new WeakReference<>(currentConnection);
		}
		if (currentConnection == null) {
			// No connection => Disregard, this probably an event being fired on the integrated server thread
			LOGGER.debug("JEI StartEventObserver received {} too early, ignoring", event.getClass());
			return false;
		}
		logReceivedEvent(event);
		return true;
	}

	private boolean shouldWaitForRecipes() {
		return serverConnection.isJeiOnServer() ||
			serverConnection.isSameModLoader();
	}

	private String getRequiredStartEventsString() {
		if (shouldWaitForRecipes()) {
			return "[%s, %s]".formatted(ClientPlayerNetworkEvent.LoggingIn.class.getName(), RecipesUpdatedEvent.class.getName());
		}
		return "[%s]".formatted(ClientPlayerNetworkEvent.LoggingIn.class.getName());
	}

	private String getMissingStartEventsString() {
		StringBuilder missingEvents = new StringBuilder("[");
		if (!observedLogin) {
			missingEvents.append(ClientPlayerNetworkEvent.LoggingIn.class.getName());
		}
		if (shouldWaitForRecipes() && !observedRecipeSync) {
			if (missingEvents.length() > 1) {
				missingEvents.append(", ");
			}
			missingEvents.append(RecipesUpdatedEvent.class.getName());
		}
		return missingEvents.append("]").toString();
	}

	private static <T extends Event> void logReceivedEvent(T event) {
		LOGGER.debug("JEI StartEventObserver received event: {}", event.getClass());
	}

	@Nullable
	private static Connection getCurrentConnection() {
		Minecraft minecraft = Minecraft.getInstance();
		ClientPacketListener packetListener = minecraft.getConnection();
		if (packetListener != null) {
			return packetListener.getConnection();
		} else if (minecraft.pendingConnection != null) {
			// Some events are fired very early in the connection process,
			// so packetListener may not be initialized.
			// Instead, we grab it from pendingConnection (singleplayer) or...
			return minecraft.pendingConnection;
		} else if (minecraft.screen instanceof ConnectScreen connectScreen) {
			//...the connect screen (multiplayer)
			return connectScreen.connection;
		} else {
			return null;
		}
	}

	@Override
	public void onResourceManagerReload(ResourceManager pResourceManager) {
		LOGGER.debug("JEI StartEventObserver detected resource manager reload.");
		restart();
	}

	private void restart() {
		if (this.state != State.JEI_STARTED) {
			return;
		}
		transitionState(State.LISTENING);
		transitionState(State.JEI_STARTED);
	}

	private void transitionState(State newState) {
		LOGGER.debug("JEI StartEventObserver transitioning state from {} to {}", this.state, newState);

		switch (newState) {
			case LISTENING -> {
				if (this.state == State.JEI_STARTED) {
					this.stopRunnable.run();
				}
			}
			case JEI_STARTED -> {
				if (this.state != State.LISTENING) {
					throw new IllegalStateException("Attempted Illegal state transition from " + this.state + " to " + newState);
				}
				this.startRunnable.run();
			}
		}

		this.state = newState;
		clearObservedStartEvents();
	}

	private void clearObservedStartEvents() {
		this.observedLogin = false;
		this.observedRecipeSync = false;
	}
}
