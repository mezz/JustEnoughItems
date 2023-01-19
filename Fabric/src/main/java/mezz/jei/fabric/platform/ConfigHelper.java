package mezz.jei.fabric.platform;

import mezz.jei.api.constants.ModIds;
import mezz.jei.common.platform.IPlatformConfigHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.Screen;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class ConfigHelper implements IPlatformConfigHelper {
    @Override
    public Path createConfigDir() {
        Path configDir = FabricLoader.getInstance()
            .getConfigDir()
            .resolve(ModIds.JEI_ID);
        try {
            Files.createDirectories(configDir);
        } catch (IOException e) {
            throw new RuntimeException("Unable to create JEI config directory: " + configDir, e);
        }
        return configDir;
    }

    @Override
    public Optional<Screen> getConfigScreen() {
        return Optional.empty();
    }
}
