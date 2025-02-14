package mezz.jei.forge.plugins.forge;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.ModIds;
import mezz.jei.api.registration.IRuntimeRegistration;
import mezz.jei.forge.events.RuntimeEventSubscriptions;
import mezz.jei.forge.startup.EventRegistration;
import mezz.jei.gui.startup.JeiEventHandlers;
import mezz.jei.gui.startup.JeiGuiStarter;
import mezz.jei.gui.startup.ResourceReloadHandler;
import mezz.jei.library.startup.ClientTaskExecutor;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@JeiPlugin
public class ForgeGuiPlugin implements IModPlugin {
    private static final Logger LOGGER = LogManager.getLogger();
    private static @Nullable ResourceReloadHandler resourceReloadHandler;

    private final RuntimeEventSubscriptions runtimeSubscriptions = new RuntimeEventSubscriptions(MinecraftForge.EVENT_BUS);

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(ModIds.JEI_ID, "forge_gui");
    }

    public void registerRuntime(IRuntimeRegistration registration, Executor executor) {
        ClientTaskExecutor.InternalExecutor internalExecutor = (ClientTaskExecutor.InternalExecutor) executor;
        internalExecutor.runAsync(new Thread(() -> {
            if (!runtimeSubscriptions.isEmpty()) {
                LOGGER.error("JEI GUI is already running.");
                runtimeSubscriptions.clear();
            }

            JeiEventHandlers eventHandlers = JeiGuiStarter.start(registration, executor);
            resourceReloadHandler = eventHandlers.resourceReloadHandler();
            EventRegistration.registerEvents(runtimeSubscriptions, eventHandlers);
        }));
    }

    @Override
    public void onRuntimeUnavailable() {
        LOGGER.info("Stopping JEI GUI");
        runtimeSubscriptions.clear();
        resourceReloadHandler = null;
    }

    public static Optional<ResourceReloadHandler> getResourceReloadHandler() {
        return Optional.ofNullable(resourceReloadHandler);
    }
}
