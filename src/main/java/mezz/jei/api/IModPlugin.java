package mezz.jei.api;

import javax.annotation.Nonnull;

/**
 * The main class for a plugin. Everything communicated between a mod and JEI is through this class.
 * IModPlugins must have the @JEIPlugin annotation to get loaded by JEI.
 * This class must not import anything that could be missing at runtime (i.e. code from any other mod).
 */
public interface IModPlugin {
	/**
	 * Register this mod plugin with the mod registry.
	 * Called just before the game launches.
	 * Will be called again if config
	 */
	void register(@Nonnull IModRegistry registry);

	/**
	 * Called when jei's runtime features are available, after all mods have registered.
	 * @since JEI 2.23.0
	 */
	void onRuntimeAvailable(@Nonnull IJeiRuntime jeiRuntime);
}
