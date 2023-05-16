package mezz.jei.fabric.plugins.fabric;

import mezz.jei.api.IRuntimePlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.ModIds;
import mezz.jei.api.registration.IRuntimeRegistration;
import mezz.jei.api.runtime.IJeiClientExecutor;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.fabric.startup.EventRegistration;
import mezz.jei.gui.startup.JeiGuiStarter;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@JeiPlugin
public class FabricRuntimePlugin implements IRuntimePlugin {
    private static final Logger LOGGER = LogManager.getLogger();
    private static @Nullable IJeiRuntime runtime;

    private final EventRegistration eventRegistration = new EventRegistration();

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(ModIds.JEI_ID, "fabric_runtime");
    }

    @Override
    public CompletableFuture<Void> registerRuntime(IRuntimeRegistration registration, IJeiClientExecutor clientExecutor) {
        return JeiGuiStarter.start(registration, clientExecutor)
            .thenAccept(eventRegistration::setEventHandlers);
    }

    @Override
    public CompletableFuture<Void> onRuntimeAvailable(IJeiRuntime jeiRuntime, IJeiClientExecutor clientExecutor) {
        runtime = jeiRuntime;
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> onRuntimeUnavailable(IJeiClientExecutor clientExecutor) {
        runtime = null;
        LOGGER.info("Stopping JEI GUI");
        eventRegistration.clear();
        return CompletableFuture.completedFuture(null);
    }

    public static Optional<IJeiRuntime> getRuntime() {
        return Optional.ofNullable(runtime);
    }
}
