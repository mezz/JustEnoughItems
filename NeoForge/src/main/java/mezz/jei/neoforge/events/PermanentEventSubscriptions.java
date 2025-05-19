package mezz.jei.neoforge.events;

import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.IModBusEvent;

import java.util.function.Consumer;

/**
 * Used for event subscriptions that are only created once and never unregistered.
 */
public class PermanentEventSubscriptions {
	private final IEventBus eventBus;
	private final IEventBus modEventBus;

	public PermanentEventSubscriptions(IEventBus eventBus, IEventBus modEventBus) {
		this.eventBus = eventBus;
		this.modEventBus = modEventBus;
	}

	public <T extends Event> void register(Class<T> eventType, Consumer<T> listener) {
		register(EventPriority.NORMAL, eventType, listener);
	}

	public <T extends Event> void register(EventPriority priority, Class<T> eventType, Consumer<T> listener) {
		if (IModBusEvent.class.isAssignableFrom(eventType)) {
			modEventBus.addListener(priority, false, eventType, listener);
		} else {
			eventBus.addListener(priority, false, eventType, listener);
		}
	}

	public IEventBus getModEventBus() {
		return modEventBus;
	}
}
