package mezz.jei.api;

import mezz.jei.api.ingredients.IModIngredientRegistration;

/**
 * The main class to implement to create a JEI plugin. Everything communicated between a mod and JEI is through this class.
 * IModPlugins must have the {@link JEIPlugin} annotation to get loaded by JEI.
 *
 * @see BlankModPlugin
 */
public interface IModPlugin {

	/**
	 * If your item has subtypes that depend on NBT or capabilities, use this to help JEI identify those subtypes correctly.
	 *
	 * @since JEI 3.12.1
	 */
	void registerItemSubtypes(ISubtypeRegistry subtypeRegistry);

	/**
	 * Register special ingredients, beyond the basic ItemStack and FluidStack.
	 *
	 * @since JEI 3.11.0
	 */
	void registerIngredients(IModIngredientRegistration registry);

	/**
	 * Register this mod plugin with the mod registry.
	 */
	void register(IModRegistry registry);

	/**
	 * Called when jei's runtime features are available, after all mods have registered.
	 *
	 * @since JEI 2.23.0
	 */
	void onRuntimeAvailable(IJeiRuntime jeiRuntime);
}
