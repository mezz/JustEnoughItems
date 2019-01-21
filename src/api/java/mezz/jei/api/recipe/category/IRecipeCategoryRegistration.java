package mezz.jei.api.recipe.category;

import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.IModPlugin;

/**
 * This is given to your {@link IModPlugin#registerCategories(IRecipeCategoryRegistration)}.
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
