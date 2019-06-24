package mezz.jei.events;

import java.util.function.Consumer;

import net.minecraftforge.fml.event.lifecycle.ModLifecycleEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;

public class EventBusHelper {
	private static IEventBus getInstance() {
		return MinecraftForge.EVENT_BUS;
	}

	public static <T extends Event> void addListener(Class<T> eventType, Consumer<T> listener) {
		IEventBus eventBus = getInstance();
		eventBus.addListener(EventPriority.NORMAL, false, eventType, listener);
	}

	public static <T extends Event> void addListener(IEventBus eventBus, Class<T> eventType, Consumer<T> listener) {
		eventBus.addListener(EventPriority.NORMAL, false, eventType, listener);
	}

	public static <T extends Event> void removeListener(Consumer<T> listener) {
		IEventBus eventBus = getInstance();
		eventBus.unregister(listener);
	}

	public static <T extends ModLifecycleEvent> void addLifecycleListener(IEventBus eventBus, Class<T> eventType, Consumer<T> listener) {
		eventBus.addListener(EventPriority.NORMAL, false, eventType, listener);
	}

	public static void register(Object object) {
		IEventBus eventBus = getInstance();
		eventBus.register(object);
	}

	public static void unregister(Object object) {
		IEventBus eventBus = getInstance();
		eventBus.unregister(object);
	}

	public static void post(Event event) {
		IEventBus eventBus = getInstance();
		eventBus.post(event);
	}
}
