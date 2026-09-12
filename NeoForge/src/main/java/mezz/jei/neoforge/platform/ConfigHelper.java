package mezz.jei.neoforge.platform;

import mezz.jei.common.platform.IPlatformConfigHelper;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
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
			.flatMap(modContainer -> modContainer.getCustomExtension(IConfigScreenFactory.class)
				.map(factory -> factory.createScreen(modContainer, parent)));
	}
}
