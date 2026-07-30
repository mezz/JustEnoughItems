package mezz.jei.fabric.events;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.server.packs.resources.ReloadableResourceManager;

public class JeiLifecycleEvents {
	public static final Event<Runnable> GAME_STOP = createRunnableEvent();

	public static final Event<Runnable> AFTER_RECIPES_UPDATED = createRunnableEvent();

	public static final Event<RegisterResourceReloadListener> REGISTER_RESOURCE_RELOAD_LISTENER = createResourceReloadListenerEvent();

	private static Event<Runnable> createRunnableEvent() {
		return EventFactory.createArrayBacked(Runnable.class, JeiLifecycleEvents::createRunnableInvoker);
	}

	private static Runnable createRunnableInvoker(Runnable[] callbacks) {
		return () -> {
			for (Runnable callback : callbacks) {
				callback.run();
			}
		};
	}

	private static Event<RegisterResourceReloadListener> createResourceReloadListenerEvent() {
		return EventFactory.createArrayBacked(RegisterResourceReloadListener.class, JeiLifecycleEvents::createResourceReloadListenerInvoker);
	}

	private static RegisterResourceReloadListener createResourceReloadListenerInvoker(RegisterResourceReloadListener[] callbacks) {
		return (resourceManager, textureManager) -> {
			for (RegisterResourceReloadListener callback : callbacks) {
				callback.registerResourceReloadListener(resourceManager, textureManager);
			}
		};
	}

	@Environment(EnvType.CLIENT)
	@FunctionalInterface
	public interface RegisterResourceReloadListener {
		void registerResourceReloadListener(ReloadableResourceManager resourceManager, TextureManager textureManager);
	}
}
