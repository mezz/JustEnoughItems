package mezz.jei.api.runtime;

import javax.annotation.Nullable

import mezz.jei.api.IModPlugin;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.transfer.IRecipeTransferManager;
import mezz.jei.api.runtime.config.IJeiConfigManager;
import mezz.jei.api.recipe.advanced.IRecipeManagerPlugin;

/**
 * Gives access to JEI functions that are available once everything has loaded.
 * The IJeiRuntime instance is passed to your mod plugin in {@link IModPlugin#onRuntimeAvailable(IJeiRuntime)}.
 */
public interface IJeiRuntime {
	/**
	 * The {@link IRecipeManager} offers several functions for retrieving and handling recipes.
	 */
	IRecipeManager getRecipeManager();

	/**
	 * The {@link IRecipesGui} is JEI's gui for displaying recipes.
	 * Use this interface to open the gui and display recipes.
	 */
	IRecipesGui getRecipesGui();

	/**
	 * The {@link IIngredientFilter} is JEI's filter that can be set by players or controlled by mods.
	 * Use this interface to get information from and interact with it.
	 */
	IIngredientFilter getIngredientFilter();

	/**
	 * The {@link IIngredientListOverlay} is JEI's gui that displays all the ingredients next to an open container gui.
	 * Use this interface to get information from and interact with it.
	 */
	IIngredientListOverlay getIngredientListOverlay();

	/**
	 * The {@link IBookmarkOverlay} is JEI's gui that displays all the bookmarked ingredients next to an open container gui.
	 * Use this interface to get information from it.
	 */
	IBookmarkOverlay getBookmarkOverlay();

	/**
	 * {@link IJeiHelpers} provides helpers and tools for addon mods.
	 *
	 * @since 9.4.2
	 */
	IJeiHelpers getJeiHelpers();

	/**
	 * The {@link IIngredientManager} has some useful functions related to recipe ingredients.
	 */
	IIngredientManager getIngredientManager();

	/**
	 * The {@link IIngredientVisibility} allows mod plugins to do advanced filtering of
	 * ingredients based on what is visible in JEI.
	 *
	 * @since 9.3.0
	 */
	IIngredientVisibility getIngredientVisibility();

	/**
	 * The {@link IJeiKeyMappings} gives access to key mappings used by JEI.
	 * This can be used by mods that want to use the same keys that players bind for JEI.
	 *
	 * @since 11.0.1
	 */
	IJeiKeyMappings getKeyMappings();

	/**
	 * Get a helper for all runtime Screen functions.
	 * This is used by JEI's GUI and can be used by other mods that want to use the same information from JEI.
	 *
	 * @since 11.5.0
	 */
	IScreenHelper getScreenHelper();

	/**
	 * Get a manager that holds all the registered recipe transfer handlers.
	 * This is used by JEI's GUI and can be used by other mods that want to use the same information from JEI.
	 *
	 * @since 11.5.0
	 */
	IRecipeTransferManager getRecipeTransferManager();

	/**
	 * Get access to the edit-mode config, which lets users hide ingredients from JEI.
	 * This is used by JEI's GUI and can be used by other mods that want to use the same information from JEI.
	 *
	 * @since 11.5.0
	 */
	IEditModeConfig getEditModeConfig();

	/**
	 * Get the config manager, used for displaying or updating JEI's config files.
	 *
	 * If you need to get this config manager as soon as it is ready,
	 * override {@link IModPlugin#onConfigManagerAvailable} instead of waiting for it to be available here.
	 *
	 * @since 12.1.0
	 */
	IJeiConfigManager getConfigManager();

        /**
         * Returns the default JEI RecipeManagerPlugin
         *
         * @return returns null if you are not the default JEI runtime and don't have a IRecipeManagerPlugin of your own
        */
        @Nullable
        default IRecipeManagerPlugin getRecipeManagerPlugin() {
            return null;
        }
}
