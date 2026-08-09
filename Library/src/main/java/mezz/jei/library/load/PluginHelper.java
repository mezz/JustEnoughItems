package mezz.jei.library.load;

import mezz.jei.api.IModPlugin;
import mezz.jei.library.plugins.jei.JeiInternalPlugin;
import mezz.jei.library.plugins.vanilla.VanillaPlugin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

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
		plugins.addFirst(vanillaPlugin);

		if (jeiInternalPlugin != null) {
			plugins.remove(jeiInternalPlugin);
			plugins.add(jeiInternalPlugin);
		}
	}

	public static <T extends IModPlugin> Optional<T> getPluginWithClass(Class<? extends T> pluginClass, List<IModPlugin> modPlugins) {
		for (IModPlugin modPlugin : modPlugins) {
			if (pluginClass.isInstance(modPlugin)) {
				T cast = pluginClass.cast(modPlugin);
				return Optional.of(cast);
			}
		}
		return Optional.empty();
	}
}
