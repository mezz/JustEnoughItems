package mezz.jei.events;

import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.IModBusEvent;

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
		if (IModBusEvent.class.isAssignableFrom(eventType)) {
			modEventBus.addListener(EventPriority.NORMAL, false, eventType, listener);
		} else {
			eventBus.addListener(EventPriority.NORMAL, false, eventType, listener);
		}
	}
}
