package mezz.jei.api;

/**
 * The main class for a plugin. Everything communicated between a mod and JEI is through this class.
 * IModPlugins must have the @JEIPlugin annotation to get loaded by JEI.
 * This class must not import anything that could be missing at runtime (i.e. code from any other mod).
 */
public interface IModPlugin {
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
	 * @deprecated since JEI 2.23.0. Get the recipe registry from jeiRuntime, passed in onRuntimeAvailable
	 */
	@Deprecated
	void onRecipeRegistryAvailable(IRecipeRegistry recipeRegistry);

	/**
	 * Called when jei's runtime features are available, after all mods have registered.
	 *
	 * @since JEI 2.23.0
	 */
	void onRuntimeAvailable(IJeiRuntime jeiRuntime);
}
