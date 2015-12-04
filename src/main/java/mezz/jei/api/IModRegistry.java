package mezz.jei.api;

import javax.annotation.Nonnull;
import java.util.List;

import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeTransferHelper;

/**
 * Passed to IModPlugins so they can register themselves.
 */
public interface IModRegistry {

	/**
	 * Add the recipe categories provided by this plugin.
	 */
	void addRecipeCategories(@Nonnull IRecipeCategory... recipeCategories);

	/**
	 * Add the recipe handlers provided by this plugin.
	 */
	void addRecipeHandlers(@Nonnull IRecipeHandler... recipeHandlers);

	/**
	 * Add the recipe transfer helpers provided by the plugin.
	 */
	void addRecipeTransferHelpers(@Nonnull IRecipeTransferHelper... recipeTransferHelpers);

	/**
	 * Add the recipes provided by the plugin.
	 * These can be regular recipes, they will get wrapped by the provided recipe handlers.
	 * Recipes that are already registered with minecraft's recipe managers don't need to be added here.
	 */
	void addRecipes(@Nonnull List<Object> recipes);
}
