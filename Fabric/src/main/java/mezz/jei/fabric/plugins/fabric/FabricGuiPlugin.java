package mezz.jei.fabric.plugins.fabric;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.ModIds;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.common.Internal;
import mezz.jei.common.config.InternalKeyMappings;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.common.startup.JeiEventHandlers;
import mezz.jei.fabric.startup.EventRegistration;
import mezz.jei.gui.startup.JeiGuiStarter;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@JeiPlugin
public class FabricGuiPlugin implements IModPlugin {
    private static final Logger LOGGER = LogManager.getLogger();

    private final EventRegistration eventRegistration = new EventRegistration();

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(ModIds.JEI_ID, "fabric_gui");
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        IConnectionToServer serverConnection = Internal.getServerConnection();
        Textures textures = Internal.getTextures();
        InternalKeyMappings keyMappings = Internal.getKeyMappings();

        JeiEventHandlers eventHandlers = JeiGuiStarter.start(jeiRuntime, serverConnection, textures, keyMappings);

        eventRegistration.setEventHandlers(eventHandlers);
    }

    @Override
    public void onRuntimeUnavailable() {
        LOGGER.info("Stopping JEI GUI");
        eventRegistration.clear();
    }
}
