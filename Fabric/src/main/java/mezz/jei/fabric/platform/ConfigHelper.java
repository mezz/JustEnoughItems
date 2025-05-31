package mezz.jei.fabric.platform;

import mezz.jei.api.constants.ModIds;
import mezz.jei.common.platform.IPlatformConfigHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.gui.screens.Screen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Optional;

public class ConfigHelper implements IPlatformConfigHelper {
	private static final Logger LOGGER = LogManager.getLogger();

	@Override
	public Path getModConfigDir() {
		return FabricLoader.getInstance()
			.getConfigDir();
	}

	@Override
	public Optional<Screen> getConfigScreen() {
		FabricLoader loader = FabricLoader.getInstance();
		if (loader.isModLoaded("configured")) {
			return loader.getModContainer(ModIds.JEI_ID)
					.flatMap(ConfigHelper::getConfiguredConfigScreen);
		}
		return Optional.empty();
	}

	private static Optional<Screen> getConfiguredConfigScreen(ModContainer jeiModContainer) {
		try {
			Class<?> configFactoryClass = Class.forName("com.mrcrayfish.configured.integration.CatalogueConfigFactory");
			Method createConfigScreen = configFactoryClass.getDeclaredMethod("createConfigScreen", Screen.class, ModContainer.class);
			Object screen = createConfigScreen.invoke(configFactoryClass, null, jeiModContainer);
			if (screen instanceof Screen configScreen) {
				return Optional.of(configScreen);
			}
			return Optional.empty();
		} catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
			LOGGER.error("Failed to load config screen with error:", e);
			return Optional.empty();
		}
	}
}
