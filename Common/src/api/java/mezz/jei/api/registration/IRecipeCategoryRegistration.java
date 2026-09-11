package mezz.jei.api.registration;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.category.IRecipeCategory;
import org.jetbrains.annotations.ApiStatus;

/**
 * This is given to your {@link IModPlugin#registerCategories(IRecipeCategoryRegistration)}.
 */
@ApiStatus.NonExtendable
public interface IRecipeCategoryRegistration {
	/**
	 * {@link IJeiHelpers} provides helpers and tools for addon mods.
	 */
	IJeiHelpers getJeiHelpers();

	/**
	 * Add the recipe categories provided by this plugin.
	 */
	void addRecipeCategories(IRecipeCategory<?>... recipeCategories);
}
