package mezz.jei.forge.platform;

import mezz.jei.common.platform.IPlatformConfigHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLPaths;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Optional;

public class ConfigHelper implements IPlatformConfigHelper {

	@Override
	public Path getModConfigDir() {
		return FMLPaths.CONFIGDIR.get();
	}

	@Override
	public Optional<Screen> getConfigScreen(String modId, @Nullable Screen parent) {
		return ModList.get()
			.getModContainerById(modId)
			.flatMap(modContainer -> modContainer.getCustomExtension(ConfigScreenHandler.ConfigScreenFactory.class))
			.map(ConfigScreenHandler.ConfigScreenFactory::screenFunction)
			.map(screenFactory -> screenFactory.apply(Minecraft.getInstance(), parent));
	}
}
