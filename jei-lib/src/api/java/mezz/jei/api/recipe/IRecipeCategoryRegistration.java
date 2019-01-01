package mezz.jei.api.recipe;

import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.IModPlugin;

/**
 * This is given to your {@link IModPlugin#registerCategories(IRecipeCategoryRegistration)}.
 *
 * @since JEI 4.5.0
 */
public interface IRecipeCategoryRegistration {
	/**
	 * Add the recipe categories provided by this plugin.
	 */
	void addRecipeCategories(IRecipeCategory... recipeCategories);

	/**
	 * Get helpers and tools for implementing JEI plugins.
	 */
	IJeiHelpers getJeiHelpers();
}
