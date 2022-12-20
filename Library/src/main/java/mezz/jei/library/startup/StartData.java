package mezz.jei.library.startup;

import mezz.jei.api.IModPlugin;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.network.IConnectionToServer;

import java.util.List;

public record StartData(
    List<IModPlugin> plugins,
    Textures textures,
    IConnectionToServer serverConnection,
    IInternalKeyMappings keyBindings
) {
}
