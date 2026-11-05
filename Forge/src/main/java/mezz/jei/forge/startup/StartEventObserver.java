package mezz.jei.forge.startup;

import mezz.jei.common.Internal;
import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.forge.events.PermanentEventSubscriptions;
import mezz.jei.library.plugins.vanilla.VanillaPlugin;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.Connection;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RecipesUpdatedEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.StringJoiner;

/**
 * This class observes events and determines when it's the right time to start JEI.
 *
 * JEI waits for {@link ClientPlayerNetworkEvent.LoggingIn} and client-side {@link TagsUpdatedEvent}
 * before registering recipes so plugins can resolve tag ingredients. When the connection can provide
 * server recipe content, JEI also waits for {@link RecipesUpdatedEvent}.
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
	private boolean observedTagSync;

	public StartEventObserver(IConnectionToServer serverConnection, Runnable startRunnable, Runnable stopRunnable) {
		this.serverConnection = serverConnection;
		this.startRunnable = startRunnable;
		this.stopRunnable = stopRunnable;
	}

	public void register(PermanentEventSubscriptions subscriptions) {
		subscriptions.register(EventPriority.LOWEST, ClientPlayerNetworkEvent.LoggingIn.class, this::onLoggingIn);
		subscriptions.register(EventPriority.LOWEST, RecipesUpdatedEvent.class, this::onRecipesUpdatedEvent);
		subscriptions.register(EventPriority.LOWEST, TagsUpdatedEvent.class, this::onTagsUpdatedEvent);
		subscriptions.register(ClientPlayerNetworkEvent.Clone.class, event -> VanillaPlugin.clearPlayerDependentCaches());

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

	private void onTagsUpdatedEvent(TagsUpdatedEvent event) {
		// Ignore server data loads so an integrated server cannot start JEI before its client receives tags.
		if (event.getUpdateCause() != TagsUpdatedEvent.UpdateCause.CLIENT_PACKET_RECEIVED || !observeConnectionEvent(event)) {
			return;
		}
		this.observedTagSync = true;
		startIfReady();
	}

	private void startIfReady() {
		// Wait for client tags on every connection so plugins can resolve tag ingredients.
		// This also lets the server brand arrive before we decide whether to wait for recipes.
		if (this.state != State.LISTENING || !this.observedLogin || !this.observedTagSync) {
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
			clearObservedStartEvents();
			this.currentConnection = new WeakReference<>(currentConnection);
		}
		if (currentConnection == null) {
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
			return "[%s, %s, %s]".formatted(
				ClientPlayerNetworkEvent.LoggingIn.class.getName(),
				TagsUpdatedEvent.class.getName(),
				RecipesUpdatedEvent.class.getName()
			);
		}
		return "[%s, %s]".formatted(ClientPlayerNetworkEvent.LoggingIn.class.getName(), TagsUpdatedEvent.class.getName());
	}

	private String getMissingStartEventsString() {
		StringJoiner missingEvents = new StringJoiner(", ", "[", "]");
		if (!observedLogin) {
			missingEvents.add(ClientPlayerNetworkEvent.LoggingIn.class.getName());
		}
		if (!observedTagSync) {
			missingEvents.add(TagsUpdatedEvent.class.getName());
		}
		if (shouldWaitForRecipes() && !observedRecipeSync) {
			missingEvents.add(RecipesUpdatedEvent.class.getName());
		}
		return missingEvents.toString();
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
		} else {
			return null;
		}
	}

	@Override
	public void onResourceManagerReload(ResourceManager resourceManager) {
		LOGGER.debug("JEI StartEventObserver detected resource manager reload.");
		restart();
	}

	public void restart() {
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
		this.observedTagSync = false;
	}
}
