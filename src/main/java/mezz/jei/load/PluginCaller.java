package mezz.jei.load;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import net.minecraft.resources.ResourceLocation;

import com.google.common.base.Stopwatch;
import mezz.jei.api.IModPlugin;
import mezz.jei.plugins.vanilla.VanillaPlugin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PluginCaller {
	private static final Logger LOGGER = LogManager.getLogger();

	public static void callOnPlugins(String title, List<IModPlugin> plugins, Consumer<IModPlugin> func) {
		List<IModPlugin> erroredPlugins = new ArrayList<>();

		for (IModPlugin plugin : plugins) {
			try {
				ResourceLocation pluginUid = plugin.getPluginUid();
				LOGGER.info("{}: {} ...", title, pluginUid);
				Stopwatch stopwatch = Stopwatch.createStarted();
				func.accept(plugin);
				LOGGER.info("{}: {} took {}", title, pluginUid, stopwatch);
			} catch (RuntimeException | LinkageError e) {
				if (plugin instanceof VanillaPlugin) {
					throw e;
				}
				LOGGER.error("Caught an error from mod plugin: {} {}", plugin.getClass(), plugin.getPluginUid(), e);
				erroredPlugins.add(plugin);
			}
		}
		plugins.removeAll(erroredPlugins);
	}
}
