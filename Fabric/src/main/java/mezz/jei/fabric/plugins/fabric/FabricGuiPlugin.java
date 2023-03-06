package mezz.jei.fabric.plugins.fabric;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.ModIds;
import mezz.jei.api.registration.IRuntimeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.common.platform.Services;
import mezz.jei.core.config.file.FileWatcher;
import mezz.jei.fabric.config.JeiClientConfigs;
import mezz.jei.fabric.startup.EventRegistration;
import mezz.jei.gui.startup.JeiEventHandlers;
import mezz.jei.gui.startup.JeiGuiStarter;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Optional;

@JeiPlugin
public class FabricGuiPlugin implements IModPlugin {
    private static final Logger LOGGER = LogManager.getLogger();
    private static @Nullable IJeiRuntime runtime;

    private final EventRegistration eventRegistration = new EventRegistration();
    private final FileWatcher fileWatcher = new FileWatcher("JEI GUI Config file watcher");

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(ModIds.JEI_ID, "fabric_gui");
    }

    @Override
    public void registerRuntime(IRuntimeRegistration registration) {
        Path configDir = Services.PLATFORM.getConfigHelper().createJeiConfigDir();
        JeiClientConfigs jeiClientConfigs = new JeiClientConfigs(configDir.resolve("jei-client.ini"));
        jeiClientConfigs.register(fileWatcher);

        JeiEventHandlers eventHandlers = JeiGuiStarter.start(registration, jeiClientConfigs);
        eventRegistration.setEventHandlers(eventHandlers);
        fileWatcher.start();
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
        fileWatcher.reset();
    }

    public static Optional<IJeiRuntime> getRuntime() {
        return Optional.ofNullable(runtime);
    }
}
