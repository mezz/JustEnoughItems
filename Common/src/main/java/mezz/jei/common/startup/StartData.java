package mezz.jei.common.startup;

import mezz.jei.api.IModPlugin;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.input.IKeyBindings;
import mezz.jei.common.network.IConnectionToServer;

import java.util.List;
import java.util.function.Supplier;

public record StartData(
    List<IModPlugin> plugins,
    Supplier<Textures> texturesSupplier,
    IConnectionToServer serverConnection,
    IKeyBindings keyBindings,
    ConfigData configData
) {
}
