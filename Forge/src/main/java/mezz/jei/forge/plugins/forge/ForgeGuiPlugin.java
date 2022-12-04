package mezz.jei.forge.plugins.forge;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.ModIds;
import mezz.jei.api.helpers.IColorHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.common.Internal;
import mezz.jei.common.config.InternalKeyMappings;
import mezz.jei.forge.JustEnoughItemsClient;
import mezz.jei.gui.startup.JeiGuiStarter;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.gui.startup.JeiEventHandlers;
import mezz.jei.forge.events.RuntimeEventSubscriptions;
import mezz.jei.forge.startup.EventRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@JeiPlugin
public class ForgeGuiPlugin implements IModPlugin {
    private static final Logger LOGGER = LogManager.getLogger();

    private final RuntimeEventSubscriptions runtimeSubscriptions = new RuntimeEventSubscriptions(MinecraftForge.EVENT_BUS);

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(ModIds.JEI_ID, "forge_gui");
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        if (!runtimeSubscriptions.isEmpty()) {
            LOGGER.error("JEI GUI is already running.");
            runtimeSubscriptions.clear();
        }

        IConnectionToServer serverConnection = Internal.getServerConnection();
        InternalKeyMappings keyMappings = Internal.getKeyMappings();

        Textures textures = Internal.getTextures();
        IJeiHelpers jeiHelpers = jeiRuntime.getJeiHelpers();
        IColorHelper colorHelper = jeiHelpers.getColorHelper();

        JeiEventHandlers eventHandlers = JeiGuiStarter.start(jeiRuntime, serverConnection, textures, keyMappings, colorHelper);

        EventRegistration.registerEvents(runtimeSubscriptions, eventHandlers);
    }

    @Override
    public void onRuntimeUnavailable() {
        LOGGER.info("Stopping JEI GUI");
        runtimeSubscriptions.clear();
    }
}
