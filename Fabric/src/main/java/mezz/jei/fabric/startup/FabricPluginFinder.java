package mezz.jei.fabric.startup;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.IRuntimePlugin;
import mezz.jei.library.startup.IPluginFinder;
import net.fabricmc.loader.api.EntrypointException;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.mojang.text2speech.Narrator.LOGGER;

public final class FabricPluginFinder implements IPluginFinder {
	private static final Map<Class<?>, String> entryPointKeys = Map.of(
		IModPlugin.class, "jei_mod_plugin",
		IRuntimePlugin.class, "jei_runtime_plugin"
	);

	@Override
	public <T> List<T> getPlugins(Class<T> pluginClass) {
		String entryPointKey = entryPointKeys.get(pluginClass);
		if (entryPointKey == null) {
			throw new IllegalArgumentException("FabricPluginFinder does not support " + pluginClass);
		}

		FabricLoader fabricLoader = FabricLoader.getInstance();
		List<EntrypointContainer<T>> pluginContainers = fabricLoader.getEntrypointContainers(entryPointKey, pluginClass);
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
