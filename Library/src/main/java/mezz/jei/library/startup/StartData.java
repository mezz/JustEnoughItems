package mezz.jei.library.startup;

import mezz.jei.api.IModPlugin;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.network.IConnectionToServer;

import java.util.List;

public record StartData(
    List<IModPlugin> plugins,
    IConnectionToServer serverConnection,
    IInternalKeyMappings keyBindings
) {
}
