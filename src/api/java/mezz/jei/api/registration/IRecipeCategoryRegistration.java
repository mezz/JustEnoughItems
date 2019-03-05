package mezz.jei.api.registration;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.category.IRecipeCategory;

/**
 * This is given to your {@link IModPlugin#registerCategories(IRecipeCategoryRegistration, IJeiHelpers)}.
 */
public interface IRecipeCategoryRegistration {
	/**
	 * Add the recipe categories provided by this plugin.
	 */
	void addRecipeCategories(IRecipeCategory... recipeCategories);
}
