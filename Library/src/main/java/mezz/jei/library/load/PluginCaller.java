package mezz.jei.library.load;

import com.google.common.base.Stopwatch;
import mezz.jei.api.IModPlugin;
import mezz.jei.common.Internal;
import mezz.jei.common.async.JeiStartTask;
import mezz.jei.library.startup.JeiStarter;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class PluginCaller {
	private static final Logger LOGGER = LogManager.getLogger();

	public static void callOnPlugins(String title, List<IModPlugin> plugins, Consumer<IModPlugin> func) {
		LOGGER.info("{}...", title);
		Stopwatch stopwatch = Stopwatch.createStarted();

		try (PluginCallerTimer timer = new PluginCallerTimer()) {
			List<IModPlugin> erroredPlugins = new ArrayList<>();

			for (IModPlugin plugin : plugins) {
				JeiStartTask.checkStartInterruption();
				try {
					ResourceLocation pluginUid = plugin.getPluginUid();
					timer.begin(title, pluginUid);
					if(plugin.needsLoadingOnClientThread() || isLegacyPlugin(plugin.getPluginUid())) {
						Minecraft.getInstance().executeBlocking(() -> func.accept(plugin));
					} else
						func.accept(plugin);
					timer.end();
				} catch (RuntimeException | LinkageError e) {
					LOGGER.error("Caught an error from mod plugin: {} {}", plugin.getClass(), plugin.getPluginUid(), e);
					erroredPlugins.add(plugin);
				}
			}
			plugins.removeAll(erroredPlugins);
		}

		LOGGER.info("{} took {}", title, stopwatch);
	}

	private static boolean isLegacyPlugin(ResourceLocation uid) {
		String uidString = uid.toString();
		// scales poorly if there are many plugins, but there shouldn't be as modders should support this mode
		// in the worst case, this can be cached
		return Internal.getJeiClientConfigs().getClientConfig().getAsyncCompatPluginUids().contains(uidString);
	}
}
