package mezz.jei.fabric.plugins.fabric;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.ModIds;
import mezz.jei.api.registration.IRuntimeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.fabric.startup.EventRegistration;
import mezz.jei.gui.startup.JeiEventHandlers;
import mezz.jei.gui.startup.JeiGuiStarter;
import mezz.jei.gui.startup.ResourceReloadHandler;
import mezz.jei.library.startup.ClientTaskExecutor;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@JeiPlugin
public class FabricGuiPlugin implements IModPlugin {
    private static final Logger LOGGER = LogManager.getLogger();
    private static @Nullable IJeiRuntime runtime;
    private static @Nullable ResourceReloadHandler resourceReloadHandler;

    private final EventRegistration eventRegistration = new EventRegistration();

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(ModIds.JEI_ID, "fabric_runtime");
    }

    public void registerRuntime(IRuntimeRegistration registration, Executor executor) {
        executor.execute(new Thread(() -> JeiGuiStarter.start(registration, executor).thenAccept(eventHandlers -> {
            resourceReloadHandler = eventHandlers.resourceReloadHandler();
        }), "Fabric Gui Start Thread"));
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        runtime = jeiRuntime;
    }

    @Override
    public void onRuntimeUnavailable() {
        runtime = null;
        LOGGER.info("Stopping JEI GUI");
        eventRegistration.clear();
    }

    public static Optional<IJeiRuntime> getRuntime() {
        return Optional.ofNullable(runtime);
    }

    public static Optional<ResourceReloadHandler> getResourceReloadHandler() {
        return Optional.ofNullable(resourceReloadHandler);
    }
}