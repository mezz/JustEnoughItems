package mezz.jei.api;

/**
 * The main class for a plugin. Everything communicated between a mod and JEI is through this class.
 * IModPlugins must have the @JEIPlugin annotation to get loaded by JEI.
 * This class must not import anything that could be missing at runtime (i.e. code from any other mod).
 */
public interface IModPlugin {

	/**
	 * Returns true if this plugin's mod is loaded.
	 *
	 * @deprecated handle this in register, if the mod isn't loaded then don't register anything.
	 */
	@Deprecated
	boolean isModLoaded();

	/**
	 * Called when the IJeiHelpers is available.
	 * IModPlugins should store IJeiHelpers here if they need it.
	 */
	void onJeiHelpersAvailable(IJeiHelpers jeiHelpers);

	/**
	 * Called when the IItemRegistry is available, before register.
	 */
	void onItemRegistryAvailable(IItemRegistry itemRegistry);

	/**
	 * Register this mod plugin with the mod registry.
	 * Called just before the game launches.
	 * Will be called again if config
	 */
	void register(IModRegistry registry);

	/**
	 * Called when the IRecipeRegistry is available, after all mods have registered.
	 */
	void onRecipeRegistryAvailable(IRecipeRegistry recipeRegistry);
}
