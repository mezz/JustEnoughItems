package mezz.jei.library.load;

import com.google.common.base.Stopwatch;
import mezz.jei.api.IAsyncCompatiblePlugin;
import mezz.jei.api.IModPlugin;
import mezz.jei.common.config.DebugConfig;
import mezz.jei.common.util.JeiThreadFactory;
import mezz.jei.library.plugins.vanilla.VanillaPlugin;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

public class PluginCaller {
	private static final Logger LOGGER = LogManager.getLogger();

	public static void callOnPlugins(String title, List<IModPlugin> plugins, Consumer<IModPlugin> func) {
		LOGGER.info("{}...", title);
		Stopwatch stopwatch = Stopwatch.createStarted();

		// If async loading is disabled, use simple synchronous execution
		if (!DebugConfig.isAsyncLoadingEnabled()) {
			callOnPluginsSync(title, plugins, func);
			LOGGER.info("{} took {}", title, stopwatch);
			return;
		}

		// Separate plugins into async-safe and sync-only
		List<IModPlugin> syncPlugins = new ArrayList<>();
		List<IModPlugin> asyncPlugins = new ArrayList<>();

		for (IModPlugin plugin : plugins) {
			if (plugin instanceof IAsyncCompatiblePlugin asyncPlugin && asyncPlugin.canExecuteAsync()) {
				asyncPlugins.add(plugin);
			} else {
				syncPlugins.add(plugin);
			}
		}

		// Execute sync plugins on main thread (100% backward compatible)
		for (IModPlugin plugin : syncPlugins) {
			try {
				ResourceLocation pluginUid = plugin.getPluginUid();
				func.accept(plugin);
			} catch (RuntimeException | LinkageError e) {
				if (plugin instanceof VanillaPlugin) {
					// Later plugins are going to crash if basic things added by the Vanilla Plugin are missing.
					// Better to just crash immediately, so that it doesn't hide the real problem in the logs.
					throw e;
				}
				LOGGER.error("Caught an error from mod plugin: {} {}", plugin.getClass(), plugin.getPluginUid(), e);
			}
		}

		// Execute async-safe plugins on background thread pool with optimized parallelism
		if (!asyncPlugins.isEmpty()) {
			CompletableFuture<Void> asyncTask = CompletableFuture.runAsync(() -> {
				// Use parallel execution for large plugin counts
				if (asyncPlugins.size() >= 4) {
					// Execute plugins in parallel using parallel streams
					asyncPlugins.parallelStream()
						.forEach(plugin -> {
							try {
								func.accept(plugin);
							} catch (RuntimeException | LinkageError e) {
								LOGGER.error("Caught an error from async mod plugin: {} {}",
									plugin.getClass(), plugin.getPluginUid(), e);
							}
						});
				} else {
					// Sequential execution for small plugin counts (less overhead)
					for (IModPlugin plugin : asyncPlugins) {
						try {
							func.accept(plugin);
						} catch (RuntimeException | LinkageError e) {
							LOGGER.error("Caught an error from async mod plugin: {} {}",
								plugin.getClass(), plugin.getPluginUid(), e);
						}
					}
				}
			}, JeiThreadFactory.getPluginLoaderExecutor());

			// Wait for async plugins to complete (with timeout to prevent hangs)
			try {
				asyncTask.get(30, TimeUnit.SECONDS);
			} catch (TimeoutException e) {
				LOGGER.error("Async plugin execution timed out after 30 seconds. Some plugins may not have completed registration.");
				asyncTask.cancel(true);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				LOGGER.error("Async plugin execution was interrupted", e);
			} catch (ExecutionException e) {
				LOGGER.error("Async plugin execution failed", e);
			}
		}

		LOGGER.info("{} took {}", title, stopwatch);
	}

	/**
	 * Execute all plugins synchronously on the current thread.
	 * Used when async loading is disabled or for plugins that don't support async execution.
	 */
	private static void callOnPluginsSync(String title, List<IModPlugin> plugins, Consumer<IModPlugin> func) {
		try (PluginCallerTimer timer = new PluginCallerTimer()) {
			for (IModPlugin plugin : plugins) {
				try {
					ResourceLocation pluginUid = plugin.getPluginUid();
					timer.begin(title, pluginUid);
					func.accept(plugin);
					timer.end();
				} catch (RuntimeException | LinkageError e) {
					if (plugin instanceof VanillaPlugin) {
						throw e;
					}
					LOGGER.error("Caught an error from mod plugin: {} {}", plugin.getClass(), plugin.getPluginUid(), e);
				}
			}
		}
	}
}
