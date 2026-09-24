package mezz.jei.neoforge.platform;

import mezz.jei.common.platform.IPlatformConfigHelper;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforgespi.language.IModInfo;
import org.jspecify.annotations.Nullable;

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
			.flatMap(m -> {
				IModInfo modInfo = m.getModInfo();
				return IConfigScreenFactory.getForMod(modInfo)
					.map(f -> f.createScreen(m, parent));
			});
	}
}
