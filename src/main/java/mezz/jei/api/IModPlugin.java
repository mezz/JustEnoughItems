package mezz.jei.api;

/**
 * The main class for a plugin. Everything passed from a mod into JEI is through this class.
 * IModPlugins must have the @JEIPlugin annotation to get loaded by JEI.
 * This class must not import anything that could be missing at runtime (i.e. code from any other mod).
 */
public interface IModPlugin {

	/** Returns true if this plugin's mod is loaded. */
	boolean isModLoaded();

	/** Register this mod plugin with the mod registry */
	void register(IModRegistry registry);
}
