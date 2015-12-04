package mezz.jei.api;

import javax.annotation.Nonnull;

import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeTransferHelper;

/**
 * The main class for a plugin. Everything passed from a mod into JEI is through this class.
 * IModPlugins must have the @JEIPlugin annotation to get loaded by JEI.
 * This class must not import anything that could be missing at runtime (i.e. code from any other mod).
 */
public interface IModPlugin {

	/** Returns true if this plugin's mod is loaded. */
	boolean isModLoaded();

	/** Return the recipe categories provided by this plugin. */
	@Nonnull
	Iterable<? extends IRecipeCategory> getRecipeCategories();

	/** Return the recipe handlers provided by this plugin. */
	@Nonnull
	Iterable<? extends IRecipeHandler> getRecipeHandlers();

	/** Return the recipe transfer helpers provided by this plugin. */
	@Nonnull
	Iterable<? extends IRecipeTransferHelper> getRecipeTransferHelpers();

	/**
	 * Return the recipes provided by this plugin.
	 * These can be regular recipes, they will get wrapped by the provided recipe handlers.
	 */
	@Nonnull
	Iterable<Object> getRecipes();
}
