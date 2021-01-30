package mezz.jei.startup;

import com.google.common.base.Preconditions;
import mezz.jei.api.IModPlugin;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.IResourceManager;
import net.minecraftforge.resource.IResourceType;
import net.minecraftforge.resource.ISelectiveResourceReloadListener;

import java.lang.ref.WeakReference;
import java.util.List;
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
        if (handlerRef != null) {
            handlerRef.startJEI();
        }
    }
}
