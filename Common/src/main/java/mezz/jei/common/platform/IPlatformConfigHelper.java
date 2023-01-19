package mezz.jei.common.platform;

import net.minecraft.client.gui.screens.Screen;

import java.nio.file.Path;
import java.util.Optional;

public interface IPlatformConfigHelper {
    Path createConfigDir();

    Optional<Screen> getConfigScreen();
}
