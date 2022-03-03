package mezz.jei.startup;

import mezz.jei.events.PermanentEventSubscriptions;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RecipesUpdatedEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.eventbus.api.Event;

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
public class StartEventObserver {
	private static final Set<Class<? extends Event>> requiredEvents = Set.of(TagsUpdatedEvent.class, RecipesUpdatedEvent.class);

	private final Set<Class<? extends Event>> observedEvents = new HashSet<>();
	private final Runnable start;
	private final Runnable restart;
	private boolean started = false;
	private boolean enabled = false;

	public StartEventObserver(Runnable start, Runnable restart) {
		this.start = start;
		this.restart = restart;
	}

	public void register(PermanentEventSubscriptions subscriptions) {
		requiredEvents
			.forEach(eventClass -> subscriptions.register(eventClass, this::onEvent));

		subscriptions.register(ClientPlayerNetworkEvent.LoggedInEvent.class, event -> {
			if (event.getPlayer() != null) {
				this.enabled = true;
				this.started = false;
				this.observedEvents.clear();
			}
		});

		subscriptions.register(ClientPlayerNetworkEvent.LoggedOutEvent.class, event -> {
			if (event.getPlayer() != null) {
				this.enabled = false;
				this.started = false;
				this.observedEvents.clear();
			}
		});
	}

	/**
	 * Observe an event and start JEI if we have observed all the required events.
	 */
	private <T extends Event> void onEvent(T event) {
		if (!enabled) {
			return;
		}
		Class<? extends Event> eventClass = event.getClass();
		if (!requiredEvents.contains(eventClass)) {
			return;
		}
		if (!this.observedEvents.add(eventClass)) {
			return;
		}
		if (this.observedEvents.containsAll(requiredEvents)) {
			if (!started) {
				start.run();
				started = true;
			} else {
				restart.run();
			}
			this.observedEvents.clear();
		}
	}

}
