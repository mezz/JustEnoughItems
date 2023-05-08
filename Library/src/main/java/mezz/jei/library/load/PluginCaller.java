package mezz.jei.library.load;

import com.google.common.base.Stopwatch;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.registration.JeiRegistrationDelegate;
import mezz.jei.api.registration.JeiRegistrationStep;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class PluginCaller {
	private static final Logger LOGGER = LogManager.getLogger();

	public static void callOnPlugins(JeiRegistrationStep step, List<IModPlugin> plugins, Consumer<IModPlugin> func) {
		LOGGER.info("{}...", step);
		Stopwatch stopwatch = Stopwatch.createStarted();

		try (PluginCallerTimer timer = new PluginCallerTimer()) {
			List<IModPlugin> erroredPlugins = new ArrayList<>();

			for (IModPlugin plugin : plugins) {
				try {
					ResourceLocation pluginUid = plugin.getPluginUid();
					timer.begin(step.toString(), pluginUid);
					JeiRegistrationDelegate.get().callOnPlugin(step, plugin, func);
					timer.end();
				} catch (RuntimeException | LinkageError e) {
					LOGGER.error("Caught an error from mod plugin: {} {}", plugin.getClass(), plugin.getPluginUid(), e);
					erroredPlugins.add(plugin);
				}
			}
			plugins.removeAll(erroredPlugins);
		}

		LOGGER.info("{} took {}", step, stopwatch);
	}
}
