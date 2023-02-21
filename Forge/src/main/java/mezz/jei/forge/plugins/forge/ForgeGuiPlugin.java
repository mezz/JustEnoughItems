package mezz.jei.forge.plugins.forge;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.ModIds;
import mezz.jei.api.registration.IRuntimeRegistration;
import mezz.jei.common.config.file.FileWatcher;
import mezz.jei.forge.events.RuntimeEventSubscriptions;
import mezz.jei.forge.startup.EventRegistration;
import mezz.jei.gui.startup.JeiEventHandlers;
import mezz.jei.gui.startup.JeiGuiStarter;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@JeiPlugin
public class ForgeGuiPlugin implements IModPlugin {
    private static final Logger LOGGER = LogManager.getLogger();

    private final RuntimeEventSubscriptions runtimeSubscriptions = new RuntimeEventSubscriptions(MinecraftForge.EVENT_BUS);
    private final FileWatcher fileWatcher = new FileWatcher("JEI GUI Config file watcher");

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(ModIds.JEI_ID, "forge_gui");
    }

    @Override
    public void registerRuntime(IRuntimeRegistration registration) {
        if (!runtimeSubscriptions.isEmpty()) {
            LOGGER.error("JEI GUI is already running.");
            runtimeSubscriptions.clear();
        }

        JeiEventHandlers eventHandlers = JeiGuiStarter.start(registration, fileWatcher);

        EventRegistration.registerEvents(runtimeSubscriptions, eventHandlers);
        fileWatcher.start();
    }

    @Override
    public void onRuntimeUnavailable() {
        LOGGER.info("Stopping JEI GUI");
        runtimeSubscriptions.clear();
        fileWatcher.reset();
    }
}
