package mezz.jei.load;

import javax.annotation.Nullable;
import java.util.List;

import mezz.jei.api.IModPlugin;
import mezz.jei.plugins.jei.JeiInternalPlugin;
import mezz.jei.plugins.vanilla.VanillaPlugin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PluginHelper {
	private static final Logger LOGGER = LogManager.getLogger();

	public static void removePluginsWithCrashingUids(List<IModPlugin> plugins) {
		plugins.removeIf(plugin -> {
			try {
				plugin.getPluginUid();
				return false;
			} catch (RuntimeException | LinkageError e) {
				LOGGER.error("Failed to get plugin UID, removing plugin from JEI: {}", plugin.getClass(), e);
				return true;
			}
		});
	}

	public static void sortPlugins(List<IModPlugin> plugins, VanillaPlugin vanillaPlugin, @Nullable JeiInternalPlugin jeiInternalPlugin) {
		plugins.remove(vanillaPlugin);
		plugins.add(0, vanillaPlugin);

		if (jeiInternalPlugin != null) {
			plugins.remove(jeiInternalPlugin);
			plugins.add(jeiInternalPlugin);
		}
	}

	@Nullable
	public static <T extends IModPlugin> T getPluginWithClass(Class<? extends T> pluginClass, List<IModPlugin> modPlugins) {
		for (IModPlugin modPlugin : modPlugins) {
			if (pluginClass.isInstance(modPlugin)) {
				return pluginClass.cast(modPlugin);
			}
		}
		return null;
	}
}
