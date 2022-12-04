package mezz.jei.fabric.plugins.fabric;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.ModIds;
import mezz.jei.api.helpers.IColorHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.common.Internal;
import mezz.jei.common.config.InternalKeyMappings;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.fabric.JustEnoughItemsClient;
import mezz.jei.gui.startup.JeiEventHandlers;
import mezz.jei.fabric.startup.EventRegistration;
import mezz.jei.gui.startup.JeiGuiStarter;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

@JeiPlugin
public class FabricGuiPlugin implements IModPlugin {
    private static final Logger LOGGER = LogManager.getLogger();
    private static @Nullable IJeiRuntime runtime;

    private final EventRegistration eventRegistration = new EventRegistration();

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(ModIds.JEI_ID, "fabric_gui");
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        runtime = jeiRuntime;
        IConnectionToServer serverConnection = Internal.getServerConnection();
        Textures textures = Internal.getTextures();
        InternalKeyMappings keyMappings = Internal.getKeyMappings();
        IJeiHelpers jeiHelpers = jeiRuntime.getJeiHelpers();
        IColorHelper colorHelper = jeiHelpers.getColorHelper();

        JeiEventHandlers eventHandlers = JeiGuiStarter.start(jeiRuntime, serverConnection, textures, keyMappings, colorHelper);

        eventRegistration.setEventHandlers(eventHandlers);
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
}
