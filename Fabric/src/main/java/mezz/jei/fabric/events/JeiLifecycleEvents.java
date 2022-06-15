package mezz.jei.fabric.events;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.server.packs.resources.ReloadableResourceManager;

public class JeiLifecycleEvents {
    public static final Event<Runnable> GAME_START =
            EventFactory.createArrayBacked(Runnable.class, callbacks -> () -> {
                for (Runnable callback : callbacks) {
                    callback.run();
                }
            });

    public static final Event<Runnable> GAME_STOP =
            EventFactory.createArrayBacked(Runnable.class, callbacks -> () -> {
                for (Runnable callback : callbacks) {
                    callback.run();
                }
            });

    public static final Event<Runnable> AFTER_RECIPE_SYNC =
            EventFactory.createArrayBacked(Runnable.class, callbacks -> () -> {
                for (Runnable callback : callbacks) {
                    callback.run();
                }
            });

    public static final Event<RegisterResourceReloadListener> REGISTER_RESOURCE_RELOAD_LISTENER =
            EventFactory.createArrayBacked(RegisterResourceReloadListener.class, callbacks -> (resourceManager, textureManager) -> {
                for (RegisterResourceReloadListener callback : callbacks) {
                    callback.registerResourceReloadListener(resourceManager, textureManager);
                }
            });

    @Environment(EnvType.CLIENT)
    @FunctionalInterface
    public interface RegisterResourceReloadListener {
        void registerResourceReloadListener(ReloadableResourceManager resourceManager, TextureManager textureManager);
    }
}
