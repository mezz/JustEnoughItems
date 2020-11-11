package mezz.jei.events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import net.minecraftforge.fml.event.lifecycle.ModLifecycleEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;

public class EventBusHelper {
    private static class Subscription {
        private final IEventBus eventBus;
        private final Consumer<? extends Event> listener;

        private <T extends Event> Subscription(IEventBus eventBus, Consumer<T> listener) {
            this.eventBus = eventBus;
            this.listener = listener;
        }
    }

    private static final Map<Object, List<Subscription>> subscriptions = new HashMap<>();

    private static IEventBus getInstance() {
        return MinecraftForge.EVENT_BUS;
    }

    /**
     * See {@link EventBusHelper#addListener(Object, IEventBus, Class, Consumer)}
     */
    public static <T extends Event> void addListener(Object owner, Class<T> eventType, Consumer<T> listener) {
        addListener(owner, getInstance(), eventType, listener);
    }

    /**
     * @param owner The owner of this listener. When the owner is unregistered, all listeners with the same owner will
     *              also be unregistered.
     */
    public static <T extends Event> void addListener(Object owner, IEventBus eventBus, Class<T> eventType, Consumer<T> listener) {
        subscriptions.computeIfAbsent(owner, e -> new ArrayList<>()).add(new Subscription(eventBus, listener));
        eventBus.addListener(EventPriority.NORMAL, false, eventType, listener);
    }

    /**
     * See {@link EventBusHelper#addListener(Object, IEventBus, Class, Consumer)}
     */
    public static <T extends ModLifecycleEvent> void addLifecycleListener(Object owner, IEventBus eventBus, Class<T> eventType, Consumer<T> listener) {
        addListener(owner, eventBus, eventType, listener);
    }

    /**
     * See {@link EventBusHelper#addListener(Object, IEventBus, Class, Consumer)}
     */
    public static <T extends Event> void removeListener(Object owner, Consumer<T> listener) {
        removeListener(owner, getInstance(), listener);
    }

    /**
     * See {@link EventBusHelper#addListener(Object, IEventBus, Class, Consumer)}
     */
    public static <T extends Event> void removeListener(Object owner, IEventBus eventBus, Consumer<T> listener) {
        subscriptions.get(owner).removeIf(sub -> Objects.equals(sub.listener, listener));
        eventBus.unregister(listener);
    }

    public static void register(Object owner) {
        IEventBus eventBus = getInstance();
        subscriptions.putIfAbsent(owner, new ArrayList<>());
        eventBus.register(owner);
    }

    /**
     * Unregisters an object from the event bus.
     * All listeners owned by this object will also be unregistered.
     * See {@link EventBusHelper#addListener(Object, IEventBus, Class, Consumer)}
     */
    public static void unregister(Object owner) {
        IEventBus eventBus = getInstance();
        for (Subscription sub : subscriptions.get(owner)) {
            removeListener(owner, sub.eventBus, sub.listener);
        }
        eventBus.unregister(owner);
        subscriptions.remove(owner);
    }

    public static void post(Event event) {
        IEventBus eventBus = getInstance();
        eventBus.post(event);
    }
}
