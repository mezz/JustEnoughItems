package mezz.jei.api;

import mezz.jei.api.ingredients.IModIngredientRegistration;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;

/**
 * The main class to implement to create a JEI plugin. Everything communicated between a mod and JEI is through this class.
 * IModPlugins must have the {@link JEIPlugin} annotation to get loaded by JEI.
 */
public interface IModPlugin {

	/**
	 * If your item has subtypes that depend on NBT or capabilities, use this to help JEI identify those subtypes correctly.
	 *
	 * @since JEI 3.12.1
	 */
	default void registerItemSubtypes(ISubtypeRegistry subtypeRegistry) {

	}

	/**
	 * Register special ingredients, beyond the basic ItemStack and FluidStack.
	 *
	 * @since JEI 3.11.0
	 */
	default void registerIngredients(IModIngredientRegistration registry) {

	}

	/**
	 * Register the categories handled by this plugin.
	 * These are registered before recipes so they can be checked for validity.
	 *
	 * @since JEI 4.5.0
	 */
	default void registerCategories(IRecipeCategoryRegistration registry) {

	}

	/**
	 * Register this mod plugin with the mod registry.
	 */
	default void register(IModRegistry registry) {

	}

	/**
	 * Called when jei's runtime features are available, after all mods have registered.
	 *
	 * @since JEI 2.23.0
	 */
	default void onRuntimeAvailable(IJeiRuntime jeiRuntime) {

	}
}
