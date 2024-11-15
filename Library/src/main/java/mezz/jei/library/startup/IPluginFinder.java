package mezz.jei.library.startup;

import java.util.List;

public interface IPluginFinder {
    <T> List<T> getPlugins(Class<T> pluginClass);
}
