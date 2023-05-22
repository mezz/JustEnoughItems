package mezz.jei.library.load;

import com.google.common.base.Stopwatch;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IRuntimePlugin;
import mezz.jei.common.async.JeiStartTask;
import mezz.jei.library.startup.ClientTaskExecutor;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

public class PluginCaller {
	private static final Logger LOGGER = LogManager.getLogger();
	private final List<IModPlugin> plugins;
	private final IRuntimePlugin runtimePlugin;
	private final ClientTaskExecutor clientExecutor;

	public PluginCaller(
		List<IModPlugin> plugins,
		IRuntimePlugin runtimePlugin,
		ClientTaskExecutor clientExecutor
	) {
		this.plugins = plugins;
		this.runtimePlugin = runtimePlugin;
		this.clientExecutor = clientExecutor;
	}

	private <T> void callSync(
		String title,
		PluginCallerTimer timer,
		List<T> plugins,
		Function<T, ResourceLocation> uidFunc,
		Consumer<T> func
	) {
		Set<T> erroredPlugins = ConcurrentHashMap.newKeySet();
		Stream<Runnable> runnables = plugins.stream()
			.map(plugin -> {
				return () -> {
					try {
						ResourceLocation pluginUid = uidFunc.apply(plugin);
						try (var ignored = timer.begin(title, pluginUid)) {
							func.accept(plugin);
						} catch (RuntimeException | LinkageError e) {
							LOGGER.error("Caught an error from mod plugin: {} {}", plugin.getClass(), pluginUid, e);
							erroredPlugins.add(plugin);
						}
					} catch (RuntimeException e) {
						LOGGER.error("Caught an error from mod plugin: {}", plugin.getClass(), e);
						erroredPlugins.add(plugin);
					}
				};
			});

		clientExecutor.runAsync(runnables);

		plugins.removeAll(erroredPlugins);
	}

	public void callOnPlugins(
		String title,
		Consumer<IModPlugin> func
	) {
		JeiStartTask.interruptIfCanceled();
		LOGGER.info("{}...", title);
		Stopwatch stopwatch = Stopwatch.createStarted();

		try (PluginCallerTimer timer = new PluginCallerTimer()) {
			callSync(
				title,
				timer,
				plugins,
				IModPlugin::getPluginUid,
				func
			);
		}

		LOGGER.info("{} took {}", title, stopwatch);
	}

	public void callOnRuntimePlugin(
		String title,
		Function<IRuntimePlugin, CompletableFuture<Void>> asyncFun
	) {
		LOGGER.info("{}...", title);
		Stopwatch stopwatch = Stopwatch.createStarted();

		try (PluginCallerTimer timer = new PluginCallerTimer()) {
			ResourceLocation pluginUid = runtimePlugin.getPluginUid();
			try (var ignored = timer.begin(title, pluginUid)) {
				clientExecutor.runAsync(() -> asyncFun.apply(runtimePlugin));
			}
		}

		LOGGER.info("{} took {}", title, stopwatch);
	}
}
