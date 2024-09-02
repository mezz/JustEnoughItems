package mezz.jei.api.registration;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.advanced.IRecipeManagerPlugin;
import mezz.jei.api.recipe.advanced.IRecipeManagerPluginHelper;
import mezz.jei.api.recipe.category.extensions.IRecipeCategoryDecorator;
import mezz.jei.api.runtime.IJeiFeatures;
import org.jetbrains.annotations.ApiStatus;

/**
 * The IAdvancedRegistration instance is passed to your mod plugin in {@link IModPlugin#registerAdvanced(IAdvancedRegistration)}.
 */
@ApiStatus.NonExtendable
public interface IAdvancedRegistration {
	/**
	 * {@link IJeiHelpers} provides helpers and tools for addon mods.
	 */
	IJeiHelpers getJeiHelpers();

	/**
	 * Helpers for implementing {@link IRecipeManagerPlugin}s.
	 *
	 * @since 11.34.8
	 */
	IRecipeManagerPluginHelper getRecipeManagerPluginHelper();

	/**
	 * Register your own {@link IRecipeManagerPlugin} here.
	 */
	void addRecipeManagerPlugin(IRecipeManagerPlugin recipeManagerPlugin);

	/**
	 * Register a {@link IRecipeCategoryDecorator} for a recipe type.
	 *
	 * @since 11.8.0
	 */
	<T> void addRecipeCategoryDecorator(RecipeType<T> recipeType, IRecipeCategoryDecorator<T> decorator);

	/**
	 * Get access to disable various JEI features.
	 * This may be needed by mods that substantially change hard-coded vanilla behaviors.
	 *
	 * @since 11.25.0
	 */
	IJeiFeatures getJeiFeatures();
}
