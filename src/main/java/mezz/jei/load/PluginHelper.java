package mezz.jei.load;

import javax.annotation.Nullable;
import java.util.List;

import mezz.jei.api.IModPlugin;
import mezz.jei.plugins.jei.JeiInternalPlugin;
import mezz.jei.plugins.vanilla.VanillaPlugin;

public class PluginHelper {
	public static void sortPlugins(List<IModPlugin> plugins, @Nullable JeiInternalPlugin jeiInternalPlugin) {
		if (jeiInternalPlugin != null) {
			plugins.remove(jeiInternalPlugin);
			plugins.add(jeiInternalPlugin);
		}
	}

	@Nullable
	public static <T> T getPluginWithClass(Class<? extends T> pluginClass, List<? super T> modPlugins) {
		for (Object modPlugin : modPlugins) {
			if (pluginClass.isInstance(modPlugin)) {
				return pluginClass.cast(modPlugin);
			}
		}
		return null;
	}
}
