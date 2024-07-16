package mezz.jei.forge.plugins.forge;

import mezz.jei.api.IRuntimePlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.ModIds;
import mezz.jei.api.registration.IRuntimeRegistration;
import mezz.jei.forge.events.RuntimeEventSubscriptions;
import mezz.jei.forge.startup.EventRegistration;
import mezz.jei.gui.startup.JeiGuiStarter;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@JeiPlugin
public class ForgeRuntimePlugin implements IRuntimePlugin {
    private static final Logger LOGGER = LogManager.getLogger();

    private final RuntimeEventSubscriptions runtimeSubscriptions = new RuntimeEventSubscriptions(MinecraftForge.EVENT_BUS);

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(ModIds.JEI_ID, "forge_runtime");
    }

    @Override
    public CompletableFuture<Void> registerRuntime(IRuntimeRegistration registration, Executor clientExecutor) {
        if (!runtimeSubscriptions.isEmpty()) {
            LOGGER.error("JEI GUI is already running.");
            runtimeSubscriptions.clear();
        }

        return JeiGuiStarter.start(registration, clientExecutor)
            .thenAcceptAsync(eventHandlers -> {
                EventRegistration.registerEvents(runtimeSubscriptions, eventHandlers);
            }, clientExecutor);
    }

    @Override
    public CompletableFuture<Void> onRuntimeUnavailable(Executor clientExecutor) {
        LOGGER.info("Stopping JEI GUI");
        runtimeSubscriptions.clear();
        return CompletableFuture.completedFuture(null);
    }
}
