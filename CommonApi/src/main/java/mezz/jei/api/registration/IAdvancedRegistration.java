package mezz.jei.api.registration;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.advanced.IRecipeManagerPlugin;

/**
 * The IAdvancedRegistration instance is passed to your mod plugin in {@link IModPlugin#registerAdvanced(IAdvancedRegistration)}.
 */
public interface IAdvancedRegistration {
	/**
	 * {@link IJeiHelpers} provides helpers and tools for addon mods.
	 */
	IJeiHelpers getJeiHelpers();

	/**
	 * Register your own {@link IRecipeManagerPlugin} here.
	 */
	void addRecipeManagerPlugin(IRecipeManagerPlugin recipeManagerPlugin);
}
