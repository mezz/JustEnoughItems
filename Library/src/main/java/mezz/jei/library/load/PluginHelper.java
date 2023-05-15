package mezz.jei.library.load;

import mezz.jei.api.IAsyncModPlugin;
import mezz.jei.library.plugins.jei.JeiInternalPlugin;
import org.jetbrains.annotations.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import mezz.jei.api.IModPlugin;
import mezz.jei.library.plugins.vanilla.VanillaPlugin;

public class PluginHelper {
	public static void sortPlugins(List<IAsyncModPlugin> plugins, VanillaPlugin vanillaPlugin, @Nullable JeiInternalPlugin jeiInternalPlugin) {
		plugins.remove(vanillaPlugin);
		plugins.add(0, vanillaPlugin);

		if (jeiInternalPlugin != null) {
			plugins.remove(jeiInternalPlugin);
			plugins.add(jeiInternalPlugin);
		}
	}

	public static <T> Optional<T> getPluginWithClass(Class<? extends T> pluginClass, List<IModPlugin> modPlugins, List<IAsyncModPlugin> asyncModPlugins) {
		return Stream.concat(
			modPlugins.stream(),
			asyncModPlugins.stream()
		)
			.filter(pluginClass::isInstance)
			.<T>map(pluginClass::cast)
			.findFirst();
	}
}
