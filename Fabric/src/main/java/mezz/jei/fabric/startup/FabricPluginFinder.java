package mezz.jei.fabric.startup;

import mezz.jei.api.IAsyncModPlugin;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IRuntimePlugin;
import mezz.jei.library.startup.IPluginFinder;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.stream.Collectors;

public final class FabricPluginFinder implements IPluginFinder {
	@Override
	public List<IModPlugin> getModPlugins() {
		return getInstances("jei_mod_plugin", IModPlugin.class);
	}

	@Override
	public List<IAsyncModPlugin> getAsyncModPlugins() {
		return getInstances("jei_async_mod_plugin", IAsyncModPlugin.class);
	}

	@Override
	public List<IRuntimePlugin> getRuntimePlugins() {
		return getInstances("jei_runtime_plugin", IRuntimePlugin.class);
	}

	@SuppressWarnings("SameParameterValue")
	private static <T> List<T> getInstances(String entrypointContainerKey, Class<T> instanceClass) {
		FabricLoader fabricLoader = FabricLoader.getInstance();
		List<EntrypointContainer<T>> pluginContainers = fabricLoader.getEntrypointContainers(entrypointContainerKey, instanceClass);
		return pluginContainers.stream()
			.<T>mapMulti((entrypointContainer, consumer) -> {
				try {
					T entrypoint = entrypointContainer.getEntrypoint();
					consumer.accept(entrypoint);
				} catch (EntrypointException e) {
					String modName;
					try {
						ModContainer provider = entrypointContainer.getProvider();
						ModMetadata metadata = provider.getMetadata();
						modName = metadata.getName();
					} catch (RuntimeException ignored) {
						modName = "unknown";
					}
					LOGGER.error("{} specified an invalid entrypoint for its JEI plugin", modName, e);
				}
			})
			.collect(Collectors.toList());
	}
}
