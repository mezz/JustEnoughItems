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
    private List<IModPlugin> plugins;

    JeiReloadListener(ClientLifecycleHandler handler, List<IModPlugin> plugins) {
        this.handler = new WeakReference<>(handler);
        this.plugins = plugins;
    }

    void update(ClientLifecycleHandler handler, List<IModPlugin> plugins) {
        this.handler = new WeakReference<>(handler);
        this.plugins = plugins;
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager, Predicate<IResourceType> resourcePredicate) {
        ClientLifecycleHandler handlerRef = handler.get();
        // check that JEI has been started before. if not, do nothing
        if (handlerRef.starter.hasStarted() && Minecraft.getInstance().world != null) {
            handlerRef.LOGGER.info("Restarting JEI.");
            Preconditions.checkNotNull(handlerRef.textures);
            handlerRef.starter.start(
                    plugins,
                    handlerRef.textures,
                    handlerRef.clientConfig,
                    handlerRef.editModeConfig,
                    handlerRef.ingredientFilterConfig,
                    handlerRef.worldConfig,
                    handlerRef.bookmarkConfig,
                    handlerRef.modIdHelper,
                    handlerRef.recipeCategorySortingConfig,
                    handlerRef.ingredientSorter
            );
        }
    }
}