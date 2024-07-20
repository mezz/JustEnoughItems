package mezz.jei.library.load;

import com.google.common.base.Stopwatch;
import mezz.jei.api.IModPlugin;
import mezz.jei.library.plugins.vanilla.VanillaPlugin;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.function.Consumer;

public class PluginCaller {
	private static final Logger LOGGER = LogManager.getLogger();

	public static void callOnPlugins(String title, List<IModPlugin> plugins, Consumer<IModPlugin> func) {
		LOGGER.info("{}...", title);
		Stopwatch stopwatch = Stopwatch.createStarted();

		try (PluginCallerTimer timer = new PluginCallerTimer()) {
			for (IModPlugin plugin : plugins) {
				try {
					ResourceLocation pluginUid = plugin.getPluginUid();
					timer.begin(title, pluginUid);
					func.accept(plugin);
					timer.end();
				} catch (RuntimeException | LinkageError e) {
					if (plugin instanceof VanillaPlugin) {
						// Later plugins are going to crash if basic things added by the Vanilla Plugin are missing.
						// Better to just crash immediately, so that it doesn't hide the real problem in the logs.
						throw e;
					}
					LOGGER.error("Caught an error from mod plugin: {} {}", plugin.getClass(), plugin.getPluginUid(), e);
				}
			}
		}

		LOGGER.info("{} took {}", title, stopwatch);
	}
}
