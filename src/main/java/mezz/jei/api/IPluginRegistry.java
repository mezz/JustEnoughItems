package mezz.jei.api;

/**
 * The main entry point for mod plugins. All plugins must register here.
 */
public interface IPluginRegistry {

	/**
	 * Register your mod plugin.
	 */
	void registerPlugin(IModPlugin plugin);

}
