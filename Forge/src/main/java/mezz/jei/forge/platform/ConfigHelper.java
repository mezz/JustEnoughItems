package mezz.jei.forge.platform;

import mezz.jei.common.platform.IPlatformConfigHelper;
import mezz.jei.gui.config.screen.JeiConfigScreen;
import net.minecraft.client.gui.screens.Screen;
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
		return Optional.of(JeiConfigScreen.create());
	}
}
