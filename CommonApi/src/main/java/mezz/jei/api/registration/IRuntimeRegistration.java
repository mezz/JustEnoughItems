package mezz.jei.api.registration;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.transfer.IRecipeTransferManager;
import mezz.jei.api.runtime.IBookmarkOverlay;
import mezz.jei.api.runtime.IEditModeConfig;
import mezz.jei.api.runtime.IIngredientFilter;
import mezz.jei.api.runtime.IIngredientListOverlay;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IRecipesGui;
import mezz.jei.api.runtime.IScreenHelper;
import mezz.jei.api.search.ISearchStorageBuilderFactory;
import mezz.jei.api.search.ISearchStorageFactory;
import org.jetbrains.annotations.ApiStatus;

/**
 * Allows mods to override the runtime classes for JEI with their own implementation.
 *
 * @since 12.0.2
 */
@ApiStatus.NonExtendable
public interface IRuntimeRegistration {
	/**
	 * Set the ingredient list overlay.
	 *
	 * This is used by JEI's GUI and can be used by other mods
	 * that want to override JEI's GUI and have it still work with mods that use JEI's API.
	 */
	void setIngredientListOverlay(IIngredientListOverlay ingredientListOverlay);

	/**
	 * Set the bookmark list overlay.
	 *
	 * This is used by JEI's GUI and can be used by other mods
	 * that want to override JEI's GUI and have it still work with mods that use JEI's API.
	 */
	void setBookmarkOverlay(IBookmarkOverlay bookmarkOverlay);

	/**
	 * Set the Recipe GUI.
	 *
	 * This is used by JEI's GUI and can be used by other mods
	 * that want to override JEI's GUI and have it still work with mods that use JEI's API.
	 */
	void setRecipesGui(IRecipesGui recipesGui);

	/**
	 * Set the Ingredient Filter.
	 *
	 * This is used by JEI's GUI and can be used by other mods
	 * that want to override JEI's GUI and have it still work with mods that use JEI's API.
	 */
	void setIngredientFilter(IIngredientFilter ingredientFilter);

	/**
	 * The {@link IRecipeManager} offers several functions for retrieving and handling recipes.
	 */
	IRecipeManager getRecipeManager();

	/**
	 * {@link IJeiHelpers} provides helpers and tools for addon mods.
	 */
	IJeiHelpers getJeiHelpers();

	/**
	 * The {@link IIngredientManager} has some useful functions related to recipe ingredients.
	 */
	IIngredientManager getIngredientManager();

	/**
	 * Get a helper for all runtime Screen functions.
	 * This is used by JEI's GUI and can be used by other mods that want to use the same information from JEI.
	 */
	IScreenHelper getScreenHelper();

	/**
	 * Get a manager that holds all the registered recipe transfer handlers.
	 * This is used by JEI's GUI and can be used by other mods that want to use the same information from JEI.
	 */
	IRecipeTransferManager getRecipeTransferManager();

	/**
	 * Get access to the edit-mode config, which lets users hide ingredients from JEI.
	 * This is used by JEI's GUI and can be used by other mods that want to use the same information from JEI.
	 */
	IEditModeConfig getEditModeConfig();

	/**
	 * Get the search storage factory used by JEI's ingredient filter.
	 * This can be overridden for advanced search behavior with
	 * {@link IModPlugin#registerAdvancedSearch(IAdvancedSearchRegistration)} and
	 * {@link IAdvancedSearchRegistration#replaceSearchStorage}.
	 *
	 * <p>
	 * This factory creates search storage directly, so the storage receives both the initial ingredient data and any
	 * runtime additions through the same live storage instance. It has no separate build step for implementations that
	 * need to preprocess or bake the initial index.
	 * </p>
	 *
	 * <p>
	 * Implementations that need to preprocess or bake the initial index can use
	 * {@link #getSearchStorageBuilderFactory()}, which exposes that initial indexing and build phase.
	 * </p>
	 *
	 * @since 27.17.0
	 * @deprecated use {@link #getSearchStorageBuilderFactory()}
	 */
	@Deprecated(since = "27.20.0", forRemoval = true)
	ISearchStorageFactory getSearchStorageFactory();

	/**
	 * Get the search storage builder factory used by JEI's ingredient filter.
	 * This can be overridden for advanced search behavior with
	 * {@link IModPlugin#registerAdvancedSearch(IAdvancedSearchRegistration)} and
	 * {@link IAdvancedSearchRegistration#replaceSearchStorage}.
	 *
	 * <p>
	 * Unlike {@link #getSearchStorageFactory()}, this returns a factory for builders instead of live search storage.
	 * JEI creates a fresh builder for each independent search index, adds the initial ingredient data to it, and then
	 * calls {@link mezz.jei.api.search.ISearchStorageBuilder#build()} to create the storage used by the ingredient
	 * filter.
	 * </p>
	 *
	 * <p>
	 * This build phase lets implementations preprocess or bake the initial index for faster searches. The storage
	 * returned by the builder is still used for runtime ingredient additions after startup.
	 * </p>
	 *
	 * @since 27.20.0
	 */
	ISearchStorageBuilderFactory getSearchStorageBuilderFactory();
}
