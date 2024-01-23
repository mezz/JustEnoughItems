package mezz.jei.neoforge.events;

import mezz.jei.core.util.WeakConsumer;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;

import java.util.function.Consumer;

public class EventSubscription<T extends Event> {
	public static <T extends Event> EventSubscription<T> register(IEventBus eventBus, Class<T> eventType, Consumer<T> listener) {
		return new EventSubscription<>(eventBus, eventType, listener);
	}

	private final IEventBus eventBus;
	/**
	 * Keep a strong reference to the listener so that
	 * it is not Garbage Collected until we stop using this subscription.
	 */
	@SuppressWarnings({"FieldCanBeLocal", "unused"})
	private final Consumer<T> listener;
	/**
	 * Workaround for https://github.com/MinecraftForge/EventBus/issues/39
	 * "Listeners are not cleaned up immediately when calling EventBus#unregister"
	 *
	 * We create a weak reference here so that the listeners retained by the Forge EventBus do not cause a memory leak.
	 */
	private final WeakConsumer<T> registeredListener;

	private EventSubscription(IEventBus eventBus, Class<T> eventType, Consumer<T> listener) {
		this.eventBus = eventBus;
		this.listener = listener;

		WeakConsumer<T> weakListener = new WeakConsumer<>(listener);
		eventBus.addListener(EventPriority.NORMAL, false, eventType, weakListener);
		this.registeredListener = weakListener;
	}

	public void unregister() {
		this.eventBus.unregister(this.registeredListener);
	}
}
