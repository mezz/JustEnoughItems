package mezz.jei.startup;

import net.minecraft.resources.IResourceManager;
import net.minecraftforge.resource.IResourceType;
import net.minecraftforge.resource.ISelectiveResourceReloadListener;

import java.lang.ref.WeakReference;
import java.util.function.Predicate;

public final class JeiReloadListener implements ISelectiveResourceReloadListener {
    private WeakReference<ClientLifecycleHandler> handler;

    JeiReloadListener(ClientLifecycleHandler handler) {
        this.handler = new WeakReference<>(handler);
    }

    void update(ClientLifecycleHandler handler) {
        this.handler = new WeakReference<>(handler);
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager, Predicate<IResourceType> resourcePredicate) {
        ClientLifecycleHandler handlerRef = handler.get();
	// Only restart JEI on a reload after initial setup
        if (handlerRef != null && handlerRef.starter.hasStarted()) {
            handlerRef.startJEI();
        }
    }
}
