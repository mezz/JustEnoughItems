package mezz.jei.events;

import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.IModBusEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Holds all the event subscriptions that JEI needs to work at runtime.
 * These will be cleared and re-registered every time a player leaves a world or enters a new one.
 */
public class RuntimeEventSubscriptions {
	private final List<EventSubscription<?>> subscriptions = new ArrayList<>();
	private final IEventBus eventBus;

	public RuntimeEventSubscriptions(IEventBus eventBus) {
		this.eventBus = eventBus;
	}

	public <T extends Event> void register(Class<T> eventType, Consumer<T> listener) {
		if (IModBusEvent.class.isAssignableFrom(eventType)) {
			throw new IllegalArgumentException(String.format("%s must be registered on the mod event bus", eventType));
		}

		EventSubscription<T> subscription = EventSubscription.register(eventBus, eventType, listener);
		this.subscriptions.add(subscription);
	}

	public boolean isEmpty() {
		return subscriptions.isEmpty();
	}

	public void clear() {
		subscriptions.forEach(EventSubscription::unregister);
		subscriptions.clear();
	}
}
