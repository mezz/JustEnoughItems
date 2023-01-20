package mezz.jei.fabric.platform;

import mezz.jei.common.platform.IPlatformConfigHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.Screen;

import java.nio.file.Path;
import java.util.Optional;

public class ConfigHelper implements IPlatformConfigHelper {
    @Override
    public Path getModConfigDir() {
        return FabricLoader.getInstance()
            .getConfigDir();
    }

    @Override
    public Optional<Screen> getConfigScreen() {
        return Optional.empty();
    }
}
