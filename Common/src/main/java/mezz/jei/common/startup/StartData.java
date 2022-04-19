package mezz.jei.common.startup;

import mezz.jei.api.IModPlugin;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.input.IKeyBindings;
import mezz.jei.common.network.IConnectionToServer;

import java.util.List;

public record StartData(
    List<IModPlugin> plugins,
    Textures textures,
    IConnectionToServer serverConnection,
    IKeyBindings keyBindings,
    ConfigData configData
) {
}
