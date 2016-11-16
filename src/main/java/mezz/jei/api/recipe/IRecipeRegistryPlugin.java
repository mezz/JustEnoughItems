package mezz.jei.api.recipe;

import java.util.List;

import mezz.jei.api.IModRegistry;
import mezz.jei.api.IRecipeRegistry;

/**
 * Recipe Registry Plugins are used by the {@link IRecipeRegistry} to look up recipes.
 * JEI has its own internal plugin, which uses information from {@link IRecipeWrapper} to look up recipes.
 * Implementing your own Recipe Registry Plugin offers total control of lookups, but it must be fast.
 * <p>
 * Add your plugin with {@link IModRegistry#addRecipeRegistryPlugin(IRecipeRegistryPlugin)}
 *
 * @since JEI 3.12.0
 */
public interface IRecipeRegistryPlugin {
	/**
	 * Returns a list of Recipe Categories offered for the focus.
	 * This is used internally by JEI to implement {@link IRecipeRegistry#getRecipeCategories(IFocus)}.
	 */
	<V> List<String> getRecipeCategoryUids(IFocus<V> focus);

	/**
	 * Returns a list of Recipe Wrappers in the recipeCategory that have the focus.
	 * This is used internally by JEI to implement {@link IRecipeRegistry#getRecipeWrappers(IRecipeCategory, IFocus)}.
	 */
	<T extends IRecipeWrapper, V> List<T> getRecipeWrappers(IRecipeCategory<T> recipeCategory, IFocus<V> focus);

	/**
	 * Returns a list of all Recipe Wrappers in the recipeCategory.
	 * This is used internally by JEI to implement {@link IRecipeRegistry#getRecipeWrappers(IRecipeCategory)}.
	 */
	<T extends IRecipeWrapper> List<T> getRecipeWrappers(IRecipeCategory<T> recipeCategory);
}
