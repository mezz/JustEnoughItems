package mezz.jei.api;

import mezz.jei.api.ingredients.IModIngredientRegistration;

/**
 * The main class to implement to create a JEI plugin. Everything communicated between a mod and JEI is through this class.
 * IModPlugins must have the {@link JEIPlugin} annotation to get loaded by JEI.
 * This class must not import anything that could be missing at runtime (i.e. code from any other mod).
 *
 * @see BlankModPlugin
 */
public interface IModPlugin {

	/**
	 * Register special ingredients, beyond the basic ItemStack and FluidStack.
	 * Called when the player joins the world and any time JEI reloads (like for a config change).
	 *
	 * @since JEI 3.11.0
	 */
	void registerIngredients(IModIngredientRegistration registry);

	/**
	 * Register this mod plugin with the mod registry.
	 * Called when the player joins the world and any time JEI reloads (like for a config change).
	 */
	void register(IModRegistry registry);

	/**
	 * Called when jei's runtime features are available, after all mods have registered.
	 * @since JEI 2.23.0
	 */
	void onRuntimeAvailable(IJeiRuntime jeiRuntime);
}
