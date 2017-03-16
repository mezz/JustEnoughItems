package mezz.jei.api;

import java.util.List;

import mezz.jei.api.recipe.IFocus;

/**
 * JEI's gui for displaying recipes. Use this interface to open recipes.
 * Get the instance from {@link IJeiRuntime#getRecipesGui()}.
 *
 * @since JEI 3.2.12
 */
public interface IRecipesGui {
	/**
	 * Show recipes for an {@link IFocus}.
	 * Opens the {@link IRecipesGui} if it is closed.
	 *
	 * @see IRecipeRegistry#createFocus(IFocus.Mode, Object)
	 * @since JEI 3.11.0
	 */
	<V> void show(IFocus<V> focus);

	/**
	 * Show entire categories of recipes.
	 *
	 * @param recipeCategoryUids a list of categories to display, in order. Must not be empty.
	 */
	void showCategories(List<String> recipeCategoryUids);
}
