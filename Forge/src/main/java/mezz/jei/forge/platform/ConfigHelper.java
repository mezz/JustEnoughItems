package mezz.jei.forge.platform;

import mezz.jei.common.platform.IPlatformConfigHelper;
import mezz.jei.gui.config.screen.JeiConfigScreen;
import net.minecraft.client.gui.screens.Screen;
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
	public Optional<Screen> getConfigScreen(@Nullable Screen parent) {
		return Optional.of(JeiConfigScreen.create(parent));
	}
}
