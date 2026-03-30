package mezz.jei.forge.startup;

import mezz.jei.forge.events.PermanentEventSubscriptions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RecipesUpdatedEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.Event;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.Set;

/**
 * This class observes events and determines when it's the right time to start JEI.
 *
 * JEI needs to see both the {@link TagsUpdatedEvent} and {@link RecipesUpdatedEvent}
 * before it is ready to start.
 *
 * Depending on the configuration (Integrated server, vanilla server, modded server),
 * these events might come in any order.
 *
 * Additionally, JEI waits for the world to finish loading before completing initialization.
 * This ensures that the world is fully loaded before JEI finishes, preventing issues with
 * world-dependent operations during JEI startup.
 */
public class StartEventObserver {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Set<Class<? extends Event>> requiredEvents = Set.of(TagsUpdatedEvent.class, RecipesUpdatedEvent.class);

	private enum State {
		DISABLED, ENABLED, EVENTS_RECEIVED, JEI_STARTED
	}

	private final Set<Class<? extends Event>> observedEvents = new HashSet<>();
	private final Runnable startRunnable;
	private final Runnable stopRunnable;
	private State state = State.DISABLED;
	private boolean worldLoaded = false;

	public StartEventObserver(Runnable startRunnable, Runnable stopRunnable) {
		this.startRunnable = startRunnable;
		this.stopRunnable = stopRunnable;
	}

	public void register(PermanentEventSubscriptions subscriptions) {
		requiredEvents
			.forEach(eventClass -> subscriptions.register(eventClass, this::onEvent));

		subscriptions.register(ClientPlayerNetworkEvent.LoggingIn.class, event -> {
			if (event.getPlayer() != null) {
				LOGGER.info("JEI StartEventObserver received {}", event.getClass());
				if (this.state == State.DISABLED) {
					transitionState(State.ENABLED);
				}
			}
		});

		subscriptions.register(ClientPlayerNetworkEvent.LoggingOut.class, event -> {
			if (event.getPlayer() != null) {
				LOGGER.info("JEI StartEventObserver received {}", event.getClass());
				transitionState(State.DISABLED);
			}
		});

		// Listen for client ticks to detect when the world is fully loaded
		subscriptions.register(TickEvent.ClientTickEvent.class, event -> {
			if (event.phase == TickEvent.Phase.START && this.state == State.EVENTS_RECEIVED) {
				Minecraft minecraft = Minecraft.getInstance();
				if (minecraft.level != null && minecraft.player != null) {
					// World is loaded and player is ready
					worldLoaded = true;
					LOGGER.info("JEI StartEventObserver: World is fully loaded");
					transitionState(State.JEI_STARTED);
				}
			}
		});

		subscriptions.register(ScreenEvent.Init.Pre.class, event -> {
			if (this.state != State.JEI_STARTED) {
				Screen screen = event.getScreen();
				Minecraft minecraft = screen.getMinecraft();
				if (screen instanceof AbstractContainerScreen && minecraft != null && minecraft.player != null) {
					LOGGER.error("""
							A Screen is opening but JEI hasn't started yet.
							Normally, JEI is started after ClientPlayerNetworkEvent.LoggedInEvent, TagsUpdatedEvent, and RecipesUpdatedEvent.
							Something has caused one or more of these events to fail, so JEI is starting very late.""");
					transitionState(State.DISABLED);
					transitionState(State.ENABLED);
					transitionState(State.JEI_STARTED);
				}
			}
		});
	}

	/**
	 * Observe an event and start JEI if we have observed all the required events.
	 * JEI will wait for the world to finish loading before completing initialization.
	 */
	private <T extends Event> void onEvent(T event) {
		if (this.state == State.DISABLED) {
			return;
		}
		LOGGER.info("JEI StartEventObserver received {}", event.getClass());
		Class<? extends Event> eventClass = event.getClass();
		if (requiredEvents.contains(eventClass) &&
			observedEvents.add(eventClass) &&
			observedEvents.containsAll(requiredEvents)
		) {
			if (this.state == State.JEI_STARTED) {
				restart();
			} else {
				// All required events received, but wait for world load
				transitionState(State.EVENTS_RECEIVED);
				LOGGER.info("JEI StartEventObserver: All required events received, waiting for world load...");

				// Check if world is already loaded (edge case)
				Minecraft minecraft = Minecraft.getInstance();
				if (minecraft.level != null && minecraft.player != null) {
					worldLoaded = true;
					transitionState(State.JEI_STARTED);
				}
			}
		}
	}

	private void restart() {
		if (this.state != State.JEI_STARTED) {
			return;
		}
		transitionState(State.DISABLED);
		transitionState(State.ENABLED);
		transitionState(State.JEI_STARTED);
	}

	private void transitionState(State newState) {
		LOGGER.info("JEI StartEventObserver transitioning state from " + this.state + " to " + newState);

		switch (newState) {
			case DISABLED -> {
				if (this.state == State.JEI_STARTED) {
					this.stopRunnable.run();
				}
				this.worldLoaded = false;
			}
			case ENABLED -> {
				if (this.state != State.DISABLED) {
					throw new IllegalStateException("Attempted Illegal state transition from " + this.state + " to " + newState);
				}
			}
			case EVENTS_RECEIVED -> {
				if (this.state != State.ENABLED) {
					throw new IllegalStateException("Attempted Illegal state transition from " + this.state + " to " + newState);
				}
				// Wait for world load before starting JEI
			}
			case JEI_STARTED -> {
				if (this.state != State.ENABLED && this.state != State.EVENTS_RECEIVED) {
					throw new IllegalStateException("Attempted Illegal state transition from " + this.state + " to " + newState);
				}
				if (this.state == State.EVENTS_RECEIVED && !worldLoaded) {
					// Not ready yet, wait for client tick
					return;
				}
				this.startRunnable.run();
				LOGGER.info("JEI has finished initializing. Mods can now access the JEI runtime via IModPlugin.onRuntimeAvailable().");
			}
		}

		this.state = newState;
		this.observedEvents.clear();
	}
}
