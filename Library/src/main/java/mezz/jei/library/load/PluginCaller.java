package mezz.jei.library.load;

import com.google.common.base.Stopwatch;
import mezz.jei.api.IAsyncModPlugin;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IRuntimePlugin;
import mezz.jei.common.async.JeiStartTask;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.library.startup.JeiClientExecutor;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
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
	private final List<IAsyncModPlugin> asyncPlugins;
	private final IRuntimePlugin runtimePlugin;
	private final JeiClientExecutor clientExecutor;
	private final IClientConfig clientConfig;

	public PluginCaller(
		List<IModPlugin> plugins,
		List<IAsyncModPlugin> asyncPlugins,
		IRuntimePlugin runtimePlugin,
		JeiClientExecutor clientExecutor,
		IClientConfig clientConfig
	) {
		this.plugins = plugins;
		this.asyncPlugins = asyncPlugins;
		this.runtimePlugin = runtimePlugin;
		this.clientExecutor = clientExecutor;
		this.clientConfig = clientConfig;
	}

	private <T> void callAsync(
		String title,
		PluginCallerTimer timer,
		List<T> plugins,
		Function<T, ResourceLocation> uidFunc,
		Function<T, CompletableFuture<Void>> func
	) {
		Set<T> erroredPlugins = ConcurrentHashMap.newKeySet();

		Stream<CompletableFuture<Void>> futures = plugins.stream()
			.map(plugin ->
				CompletableFuture.<CompletableFuture<Void>>supplyAsync(() -> {
						JeiStartTask.interruptIfCanceled();
						ResourceLocation pluginUid = uidFunc.apply(plugin);
						var t = timer.begin(title, pluginUid);
						try {
							return func.apply(plugin)
								.handle((v, e) -> {
									if (e != null) {
										LOGGER.error("Caught an error from mod plugin: {} {}", plugin.getClass(), pluginUid, e);
									}
									t.close();
									return null;
								});
						} catch (RuntimeException | LinkageError e) {
							LOGGER.error("Caught an error from mod plugin: {} {}", plugin.getClass(), pluginUid, e);
							erroredPlugins.add(plugin);
							t.close();
							return CompletableFuture.completedFuture(null);
						}
					})
					.thenCompose(f -> f)
			);

		CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));

		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.isSameThread()) {
			minecraft.managedBlock(() -> {
				if (allFutures.isDone()) {
					return true;
				}
				clientExecutor.tick();
				return false;
			});
		}
		allFutures.join();

		plugins.removeAll(erroredPlugins);
	}

	private <T> void callSync(
		String title,
		PluginCallerTimer timer,
		List<T> plugins,
		Function<T, ResourceLocation> uidFunc,
		Consumer<T> func
	) {
		Set<T> erroredPlugins = ConcurrentHashMap.newKeySet();
		for (T plugin : plugins) {
			ResourceLocation pluginUid = uidFunc.apply(plugin);
			try (var ignored = timer.begin(title, pluginUid)) {
				func.accept(plugin);
			} catch (RuntimeException | LinkageError e) {
				LOGGER.error("Caught an error from mod plugin: {} {}", plugin.getClass(), pluginUid, e);
				erroredPlugins.add(plugin);
			}
		}
		plugins.removeAll(erroredPlugins);
	}

	public void callOnPlugins(
		String title,
		Consumer<IModPlugin> func,
		Function<IAsyncModPlugin, CompletableFuture<Void>> asyncFun
	) {
		LOGGER.info("{}...", title);
		Stopwatch stopwatch = Stopwatch.createStarted();

		try (PluginCallerTimer timer = new PluginCallerTimer()) {
			callAsync(
				title,
				timer,
				asyncPlugins,
				IAsyncModPlugin::getPluginUid,
				asyncFun
			);

			if (clientConfig.getParallelPluginLoadingEnabled()) {
				callAsync(
					title,
					timer,
					plugins,
					IModPlugin::getPluginUid,
					p -> CompletableFuture.runAsync(() -> func.accept(p))
				);
			} else {
				clientExecutor.runOnClientThread(() -> {
					callSync(
						title,
						timer,
						plugins,
						IModPlugin::getPluginUid,
						func
					);
				}).join();
			}
		}

		LOGGER.info("{} took {}", title, stopwatch);
	}

	public void callOnRuntimePlugin(
		String title,
		Function<IRuntimePlugin, CompletableFuture<Void>> asyncFun
	) {
		LOGGER.info("{}...", title);
		Stopwatch stopwatch = Stopwatch.createStarted();

		List<IRuntimePlugin> runtimePlugins = new ArrayList<>();
		runtimePlugins.add(runtimePlugin);
		try (PluginCallerTimer timer = new PluginCallerTimer()) {
			callAsync(
				title,
				timer,
				runtimePlugins,
				IRuntimePlugin::getPluginUid,
				asyncFun
			);
		}

		LOGGER.info("{} took {}", title, stopwatch);
	}
}
