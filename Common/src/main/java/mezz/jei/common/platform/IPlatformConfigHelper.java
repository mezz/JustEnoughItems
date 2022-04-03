package mezz.jei.common.platform;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Optional;

public interface IPlatformConfigHelper {
    Optional<Screen> getConfigScreen();

    Component getMissingConfigScreenMessage();
}
