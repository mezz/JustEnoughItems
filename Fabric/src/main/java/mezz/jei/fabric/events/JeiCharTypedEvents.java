package mezz.jei.fabric.events;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.input.CharacterEvent;

public class JeiCharTypedEvents {
	public static final Event<BeforeCharTyped> BEFORE_CHAR_TYPED = createBeforeCharTypedEvent();

	public static final Event<AfterCharTyped> AFTER_CHAR_TYPED = createAfterCharTypedEvent();

	private static Event<BeforeCharTyped> createBeforeCharTypedEvent() {
		return EventFactory.createArrayBacked(BeforeCharTyped.class, JeiCharTypedEvents::createBeforeCharTypedInvoker);
	}

	private static BeforeCharTyped createBeforeCharTypedInvoker(BeforeCharTyped[] callbacks) {
		return (windowHandle, event) -> {
			for (BeforeCharTyped callback : callbacks) {
				if (callback.beforeCharTyped(windowHandle, event)) {
					return true;
				}
			}
			return false;
		};
	}

	private static Event<AfterCharTyped> createAfterCharTypedEvent() {
		return EventFactory.createArrayBacked(AfterCharTyped.class, JeiCharTypedEvents::createAfterCharTypedInvoker);
	}

	private static AfterCharTyped createAfterCharTypedInvoker(AfterCharTyped[] callbacks) {
		return (windowHandle, event) -> {
			for (AfterCharTyped callback : callbacks) {
				if (callback.afterCharTyped(windowHandle, event)) {
					return true;
				}
			}
			return false;
		};
	}

	@Environment(EnvType.CLIENT)
	@FunctionalInterface
	public interface BeforeCharTyped {
		boolean beforeCharTyped(long windowHandle, CharacterEvent event);
	}

	@Environment(EnvType.CLIENT)
	@FunctionalInterface
	public interface AfterCharTyped {
		boolean afterCharTyped(long windowHandle, CharacterEvent event);
	}
}
