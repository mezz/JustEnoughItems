package mezz.jei.load;

import org.jetbrains.annotations.Nullable;
import java.util.List;

import mezz.jei.api.IModPlugin;
import mezz.jei.plugins.jei.JeiInternalPlugin;
import mezz.jei.plugins.vanilla.VanillaPlugin;

public class PluginHelper {
	public static void sortPlugins(List<IModPlugin> plugins, VanillaPlugin vanillaPlugin, @Nullable JeiInternalPlugin jeiInternalPlugin) {
		plugins.remove(vanillaPlugin);
		plugins.add(0, vanillaPlugin);

		if (jeiInternalPlugin != null) {
			plugins.remove(jeiInternalPlugin);
			plugins.add(jeiInternalPlugin);
		}
	}

	@Nullable
	public static <T extends IModPlugin> T getPluginWithClass(Class<? extends T> pluginClass, List<IModPlugin> modPlugins) {
		for (IModPlugin modPlugin : modPlugins) {
			if (pluginClass.isInstance(modPlugin)) {
				return pluginClass.cast(modPlugin);
			}
		}
		return null;
	}
}
