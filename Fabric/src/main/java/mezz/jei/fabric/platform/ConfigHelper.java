package mezz.jei.fabric.platform;

import mezz.jei.common.platform.IPlatformConfigHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.Screen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Optional;

public class ConfigHelper implements IPlatformConfigHelper {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final String MOD_MENU_MOD_ID = "modmenu";
	private static final String MEZZ_CONFIG_GUI_MOD_ID = "mezz_config_gui";

	@Override
	public Path getModConfigDir() {
		return FabricLoader.getInstance()
			.getConfigDir();
	}

	@Override
	public Optional<Screen> getConfigScreen(String modId, @Nullable Screen parent) {
		FabricLoader loader = FabricLoader.getInstance();
		if (loader.isModLoaded(MEZZ_CONFIG_GUI_MOD_ID)) {
			Optional<Screen> configScreen = getMezzConfigScreen(modId, parent);
			if (configScreen.isPresent()) {
				return configScreen;
			}
		}
		if (loader.isModLoaded(MOD_MENU_MOD_ID)) {
			return getModMenuConfigScreen(modId, parent);
		}
		return Optional.empty();
	}

	private static Optional<Screen> getMezzConfigScreen(String modId, @Nullable Screen parent) {
		try {
			return MezzConfigGuiHelper.getConfigScreen(modId, parent);
		} catch (RuntimeException | LinkageError e) {
			LOGGER.error("Failed to load the MezzConfig GUI config screen:", e);
			return Optional.empty();
		}
	}

	private static Optional<Screen> getModMenuConfigScreen(String modId, @Nullable Screen parent) {
		try {
			Class<?> modMenuClass = Class.forName("com.terraformersmc.modmenu.ModMenu");
			Method getConfigScreen = modMenuClass.getMethod("getConfigScreen", String.class, Screen.class);
			Object screen = getConfigScreen.invoke(null, modId, parent);
			if (screen instanceof Screen configScreen) {
				return Optional.of(configScreen);
			}
			return Optional.empty();
		} catch (ReflectiveOperationException | LinkageError e) {
			LOGGER.error("Failed to load the Mod Menu config screen:", e);
			return Optional.empty();
		}
	}
}
