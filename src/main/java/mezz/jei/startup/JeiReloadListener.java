package mezz.jei.startup;

import com.google.common.base.Preconditions;
import mezz.jei.api.IModPlugin;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.IResourceManager;
import net.minecraftforge.resource.IResourceType;
import net.minecraftforge.resource.ISelectiveResourceReloadListener;

import java.util.List;
import java.util.function.Predicate;

public final class JeiReloadListener implements ISelectiveResourceReloadListener {
    private ClientLifecycleHandler handler;
    private List<IModPlugin> plugins;

    JeiReloadListener(ClientLifecycleHandler handler, List<IModPlugin> plugins) {
        this.handler = handler;
        this.plugins = plugins;
    }

    void update(ClientLifecycleHandler handler, List<IModPlugin> plugins) {
        this.handler = handler;
        this.plugins = plugins;
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager, Predicate<IResourceType> resourcePredicate) {
        // check that JEI has been started before. if not, do nothing
        if (handler.starter.hasStarted() && Minecraft.getInstance().world != null) {
            handler.LOGGER.info("Restarting JEI.");
            Preconditions.checkNotNull(handler.textures);
            handler.starter.start(
                    plugins,
                    handler.textures,
                    handler.clientConfig,
                    handler.editModeConfig,
                    handler.ingredientFilterConfig,
                    handler.worldConfig,
                    handler.bookmarkConfig,
                    handler.modIdHelper,
                    handler.recipeCategorySortingConfig,
                    handler.ingredientSorter
            );
        }
    }
}