package mezz.jei.fabric.platform;

import mezz.jei.common.platform.IPlatformConfigHelper;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

import java.util.Optional;

public class ConfigHelper implements IPlatformConfigHelper {
    @Override
    public Optional<Screen> getConfigScreen() {
        return Optional.empty();
    }

    @Override
    public Component getMissingConfigScreenMessage() {
        return new TextComponent("A JEI Config screen has not been implemented for Fabric");
    }
}
