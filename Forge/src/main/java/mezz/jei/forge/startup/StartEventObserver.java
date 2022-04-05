package mezz.jei.forge.startup;

import mezz.jei.forge.events.PermanentEventSubscriptions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RecipesUpdatedEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
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
 */
public class StartEventObserver implements ResourceManagerReloadListener {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Set<Class<? extends Event>> requiredEvents = Set.of(TagsUpdatedEvent.class, RecipesUpdatedEvent.class);

	private enum State {
		DISABLED, ENABLED, JEI_STARTED
	}

	private final Set<Class<? extends Event>> observedEvents = new HashSet<>();
	private final Runnable startRunnable;
	private final Runnable stopRunnable;
	private State state = State.DISABLED;

	public StartEventObserver(Runnable startRunnable, Runnable stopRunnable) {
		this.startRunnable = startRunnable;
		this.stopRunnable = stopRunnable;
	}

	public void register(PermanentEventSubscriptions subscriptions) {
		requiredEvents
			.forEach(eventClass -> subscriptions.register(eventClass, this::onEvent));

		subscriptions.register(ClientPlayerNetworkEvent.LoggedInEvent.class, event -> {
			if (event.getPlayer() != null) {
				LOGGER.info("JEI StartEventObserver received {}", event.getClass());
				if (this.state == State.DISABLED) {
					transitionState(State.ENABLED);
				}
			}
		});

		subscriptions.register(ClientPlayerNetworkEvent.LoggedOutEvent.class, event -> {
			if (event.getPlayer() != null) {
				LOGGER.info("JEI StartEventObserver received {}", event.getClass());
				transitionState(State.DISABLED);
			}
		});

		subscriptions.register(ScreenEvent.InitScreenEvent.Pre.class, event -> {
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
				transitionState(State.JEI_STARTED);
			}
		}
	}

	@Override
	public void onResourceManagerReload(ResourceManager pResourceManager) {
		restart();
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
			}
			case ENABLED -> {
				if (this.state != State.DISABLED) {
					throw new IllegalStateException("Attempted Illegal state transition from " + this.state + " to " + newState);
				}
			}
			case JEI_STARTED -> {
				if (this.state != State.ENABLED) {
					throw new IllegalStateException("Attempted Illegal state transition from " + this.state + " to " + newState);
				}
				this.startRunnable.run();
			}
		}

		this.state = newState;
		this.observedEvents.clear();
	}
}
