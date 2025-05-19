package mezz.jei.api.registration;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.advanced.IRecipeManagerPlugin;
import mezz.jei.api.recipe.advanced.IRecipeManagerPluginHelper;
import mezz.jei.api.recipe.advanced.ISimpleRecipeManagerPlugin;
import mezz.jei.api.recipe.category.extensions.IRecipeCategoryDecorator;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.runtime.IJeiFeatures;

/**
 * The IAdvancedRegistration instance is passed to your mod plugin in {@link IModPlugin#registerAdvanced(IAdvancedRegistration)}.
 */
public interface IAdvancedRegistration {
	/**
	 * {@link IJeiHelpers} provides helpers and tools for addon mods.
	 */
	IJeiHelpers getJeiHelpers();

	/**
	 * Helpers for implementing {@link IRecipeManagerPlugin}s.
	 *
	 * @since 19.15.1
	 */
	IRecipeManagerPluginHelper getRecipeManagerPluginHelper();

	/**
	 * Register your own {@link IRecipeManagerPlugin} here.
	 */
	void addRecipeManagerPlugin(IRecipeManagerPlugin recipeManagerPlugin);

	/**
	 * Register your own {@link ISimpleRecipeManagerPlugin} here.
	 *
	 * @since 20.0.0
	 */
	<T> void addSimpleRecipeManagerPlugin(IRecipeType<T> recipeType, ISimpleRecipeManagerPlugin<T> recipeManagerPlugin);

	/**
	 * Register your own {@link ISimpleRecipeManagerPlugin} here.
	 *
	 * @since 19.16.0
	 * @deprecated use {@link #addSimpleRecipeManagerPlugin(IRecipeType, ISimpleRecipeManagerPlugin)} instead.
	 */
	@Deprecated(forRemoval = true, since = "20.0.0")
	default <T> void addTypedRecipeManagerPlugin(IRecipeType<T> recipeType, ISimpleRecipeManagerPlugin<T> recipeManagerPlugin) {
		addSimpleRecipeManagerPlugin(recipeType, recipeManagerPlugin);
	}

	/**
	 * Register a {@link IRecipeCategoryDecorator} for a recipe type.
	 *
	 * @since 15.1.0
	 */
	<T> void addRecipeCategoryDecorator(IRecipeType<T> recipeType, IRecipeCategoryDecorator<T> decorator);

	/**
	 * Get access to disable various JEI features.
	 * This may be needed by mods that substantially change hard-coded vanilla behaviors.
	 *
	 * @since 17.3.0
	 */
	IJeiFeatures getJeiFeatures();
}
