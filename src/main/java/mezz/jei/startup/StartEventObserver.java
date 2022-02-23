package mezz.jei.startup;

import mezz.jei.events.PermanentEventSubscriptions;
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

	public StartEventObserver(Runnable start) {
		this.start = start;
	}

	public void register(PermanentEventSubscriptions subscriptions) {
		requiredEvents
			.forEach(eventClass -> subscriptions.register(eventClass, this::onEvent));
	}

	/**
	 * Observe an event and start JEI if we have observed all the required events.
	 */
	private <T extends Event> void onEvent(T event) {
		Class<? extends Event> eventClass = event.getClass();
		if (requiredEvents.contains(eventClass)) {
			this.observedEvents.add(eventClass);
		}
		if (this.observedEvents.containsAll(requiredEvents)) {
			start.run();
		}
	}

	public void reset() {
		this.observedEvents.clear();
	}
}
