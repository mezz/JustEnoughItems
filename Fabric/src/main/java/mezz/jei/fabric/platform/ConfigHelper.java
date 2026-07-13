package mezz.jei.fabric.platform;

import mezz.jei.common.platform.IPlatformConfigHelper;
import mezz.jei.gui.config.screen.JeiConfigScreen;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Optional;

public class ConfigHelper implements IPlatformConfigHelper {

    @Override
    public Path getModConfigDir() {
        return FabricLoader.getInstance()
                .getConfigDir();
    }

    @Override
    public Optional<Screen> getConfigScreen(@Nullable Screen parent) {
        return Optional.of(JeiConfigScreen.create(parent));
    }
}
