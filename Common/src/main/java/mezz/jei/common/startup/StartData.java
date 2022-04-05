package mezz.jei.common.startup;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.ingredients.IIngredientSorter;
import mezz.jei.common.input.IKeyBindings;
import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.common.plugins.vanilla.VanillaPlugin;

import java.util.List;

public record StartData(
    List<IModPlugin> plugins,
    VanillaPlugin vanillaPlugin,
    Textures textures,
    IConnectionToServer serverConnection,
    IModIdHelper modIdHelper,
    IIngredientSorter ingredientSorter,
    IKeyBindings keyBindings,
    ConfigData configData
) {
}
