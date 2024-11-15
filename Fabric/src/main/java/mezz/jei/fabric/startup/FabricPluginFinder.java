package mezz.jei.fabric.startup;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.IRuntimePlugin;
import mezz.jei.library.startup.IPluginFinder;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
			.map(EntrypointContainer::getEntrypoint)
			.collect(Collectors.toList());
	}

}
