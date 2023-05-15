package mezz.jei.library.startup;

import mezz.jei.api.IAsyncModPlugin;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IRuntimePlugin;

import java.util.List;

public interface IPluginFinder {
    List<IModPlugin> getModPlugins();
    List<IAsyncModPlugin> getAsyncModPlugins();
    List<IRuntimePlugin> getRuntimePlugins();
}
