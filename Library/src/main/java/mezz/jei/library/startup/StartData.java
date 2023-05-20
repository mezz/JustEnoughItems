package mezz.jei.library.startup;

import mezz.jei.api.IAsyncModPlugin;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IRuntimePlugin;
import mezz.jei.api.constants.ModIds;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.network.IConnectionToServer;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public record StartData(
    List<IModPlugin> plugins,
    List<IAsyncModPlugin> asyncPlugins,
    IRuntimePlugin runtimePlugin,
    IConnectionToServer serverConnection,
    IInternalKeyMappings keyBindings
) {
    private static final Logger LOGGER = LogManager.getLogger();

    public static StartData create(
        IPluginFinder pluginFinder,
        IConnectionToServer connectionToServer,
        IInternalKeyMappings keyBindings
    ) {
        List<IRuntimePlugin> runtimePlugins = pluginFinder.getPlugins(IRuntimePlugin.class);
        if (runtimePlugins.size() > 1) {
            // Only one runtime plugin should be active at a time.
            // If a mod has registered one, it gets priority over JEI's.
            runtimePlugins = runtimePlugins.stream()
                .filter(r -> !r.getPluginUid().getNamespace().equals(ModIds.JEI_ID))
                .sorted()
                .toList();
        }

        IRuntimePlugin runtimePlugin = runtimePlugins.get(0);
        if (runtimePlugins.size() > 1) {
            LOGGER.warn(
                """
                    Multiple runtime plugins have been registered but only one can be used.
                    Chosen runtime plugin: {}
                    Ignored runtime plugins: [{}]""",
                runtimePlugin.getPluginUid(),
                runtimePlugins.stream()
                    .filter(r -> !Objects.equals(r, runtimePlugin))
                    .map(r -> r.getPluginUid())
                    .map(ResourceLocation::toString)
                    .collect(Collectors.joining(", "))
            );
        }

        return new StartData(
            pluginFinder.getPlugins(IModPlugin.class),
            pluginFinder.getPlugins(IAsyncModPlugin.class),
            runtimePlugin,
            connectionToServer,
            keyBindings
        );
    }
}
