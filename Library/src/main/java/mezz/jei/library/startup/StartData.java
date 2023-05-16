package mezz.jei.library.startup;

import mezz.jei.api.IAsyncModPlugin;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IRuntimePlugin;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.network.IConnectionToServer;

import java.util.List;

public record StartData(
    List<IModPlugin> plugins,
    List<IAsyncModPlugin> asyncPlugins,
    List<IRuntimePlugin> runtimePlugins,
    IConnectionToServer serverConnection,
    IInternalKeyMappings keyBindings
) {
    public static StartData create(
        IPluginFinder pluginFinder,
        IConnectionToServer connectionToServer,
        IInternalKeyMappings keyBindings
    ) {
        return new StartData(
            pluginFinder.getPlugins(IModPlugin.class),
            pluginFinder.getPlugins(IAsyncModPlugin.class),
            pluginFinder.getPlugins(IRuntimePlugin.class),
            connectionToServer,
            keyBindings
        );
    }
}
