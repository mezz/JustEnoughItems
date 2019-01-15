package mezz.jei.api;

import com.google.common.collect.ImmutableList;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.api.recipe.IIngredientType;

/**
 * The IIngredientFilter is JEI's filter that can be set by players or controlled by mods.
 * Use this interface to get information from and interact with it.
 * Get the instance from {@link IJeiRuntime#getIngredientFilter()}.
 *
 * @since JEI 4.5.0
 */
public interface IIngredientFilter {
	/**
	 * Set the search filter string for the ingredient list.
	 */
	void setFilterText(String filterText);

	/**
	 * @return the current search filter string for the ingredient list
	 */
	String getFilterText();

	/**
	 * @return a list containing all ingredients that match the current filter.
	 * To get all the ingredients known to JEI, see {@link IIngredientRegistry#getAllIngredients(IIngredientType)}.
	 */
	ImmutableList<Object> getFilteredIngredients();
}
