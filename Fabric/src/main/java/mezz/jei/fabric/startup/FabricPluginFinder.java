package mezz.jei.fabric.startup;

import mezz.jei.api.IModPlugin;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;

import java.util.List;
import java.util.stream.Collectors;

public final class FabricPluginFinder {
	private FabricPluginFinder() {

	}

	public static List<IModPlugin> getModPlugins() {
		return getInstances("jei_mod_plugin", IModPlugin.class);
	}

	@SuppressWarnings("SameParameterValue")
	private static <T> List<T> getInstances(String entrypointContainerKey, Class<T> instanceClass) {
		FabricLoader fabricLoader = FabricLoader.getInstance();
		List<EntrypointContainer<T>> pluginContainers = fabricLoader.getEntrypointContainers(entrypointContainerKey, instanceClass);
		return pluginContainers.stream()
			.map(EntrypointContainer::getEntrypoint)
			.collect(Collectors.toList());
	}
}
