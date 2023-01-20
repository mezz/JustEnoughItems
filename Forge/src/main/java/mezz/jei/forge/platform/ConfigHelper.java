package mezz.jei.forge.platform;

import mezz.jei.api.constants.ModIds;
import mezz.jei.common.platform.IPlatformConfigHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;
import java.util.Optional;

public class ConfigHelper implements IPlatformConfigHelper {

    @Override
    public Path getModConfigDir() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public Optional<Screen> getConfigScreen() {
        Minecraft minecraft = Minecraft.getInstance();
        return ModList.get()
            .getModContainerById(ModIds.JEI_ID)
            .map(ModContainer::getModInfo)
            .flatMap(ConfigScreenHandler::getScreenFactoryFor)
            .map(f -> f.apply(minecraft, minecraft.screen));
    }
}
