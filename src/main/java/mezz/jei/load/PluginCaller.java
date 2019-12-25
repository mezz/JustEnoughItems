package mezz.jei.load;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

import net.minecraft.util.ResourceLocation;

import com.google.common.base.Stopwatch;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModPluginAsync;
import mezz.jei.api.constants.ModIds;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PluginCaller {
	private static final Logger LOGGER = LogManager.getLogger();

	public static void callOnPlugins(String title, List<IModPlugin> syncPlugins, Consumer<IModPlugin> syncFunc, List<IModPluginAsync> asyncPlugins, Function<IModPluginAsync, CompletableFuture<Void>> asyncFunc) {
		callOnAsyncPlugins(title, asyncPlugins, asyncFunc);
		callOnSyncPlugins(title, syncPlugins, syncFunc);
	}

	private static void callOnAsyncPlugins(String title, List<IModPluginAsync> plugins, Function<IModPluginAsync, CompletableFuture<Void>> func) {
		List<IModPluginAsync> erroredPlugins = Collections.synchronizedList(new ArrayList<>());

		plugins.parallelStream().forEach(plugin -> {
			ResourceLocation pluginUid = plugin.getPluginUid();
			try {
				LOGGER.debug("Async {}: {} ...", title, pluginUid);
				CompletableFuture<Void> future = func.apply(plugin);
				future.join();
			} catch (RuntimeException | LinkageError e) {
				if (ModIds.JEI_ID.equals(pluginUid.getNamespace())) {
					throw e;
				}
				LOGGER.error("Caught an error from mod plugin: {} {}", plugin.getClass(), pluginUid, e);
				erroredPlugins.add(plugin);
			}
		});

		plugins.removeAll(erroredPlugins);
	}

	private static void callOnSyncPlugins(String title, List<IModPlugin> plugins, Consumer<IModPlugin> func) {
		List<IModPlugin> erroredPlugins = new ArrayList<>();

		for (IModPlugin plugin : plugins) {
			ResourceLocation pluginUid = plugin.getPluginUid();
			try {
				LOGGER.debug("{}: {} ...", title, pluginUid);
				Stopwatch stopwatch = Stopwatch.createStarted();
				func.accept(plugin);
				LOGGER.debug("{}: {} took {}", title, pluginUid, stopwatch);
			} catch (RuntimeException | LinkageError e) {
				if (ModIds.JEI_ID.equals(pluginUid.getNamespace())) {
					throw e;
				}
				LOGGER.error("Caught an error from mod plugin: {} {}", plugin.getClass(), pluginUid, e);
				erroredPlugins.add(plugin);
			}
		}
		plugins.removeAll(erroredPlugins);
	}
}
