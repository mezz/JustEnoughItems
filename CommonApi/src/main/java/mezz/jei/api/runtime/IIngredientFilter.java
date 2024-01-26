package mezz.jei.api.runtime;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientType;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * The {@link IIngredientFilter} is JEI's filter that can be set by players or controlled by mods.
 * Use this interface to get information from and interact with it.
 * Get the instance from {@link IJeiRuntime#getIngredientFilter()}.
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
	 * @return a list containing all ItemStacks that match the current filter.
	 *
	 * @see #getFilteredIngredients(IIngredientType) to get a different type of ingredient, not just ItemStack.
	 *
	 * @see	IIngredientManager#getAllIngredients(IIngredientType)
	 * to get all the ingredients known to JEI, not just ones currently shown by the filter.
	 *
	 * @since 11.1.1
	 */
	default List<ItemStack> getFilteredItemStacks() {
		return getFilteredIngredients(VanillaTypes.ITEM_STACK);
	}

	/**
	 * @return a list containing all ingredients that match the current filter.
	 *
	 * @see #getFilteredItemStacks() to just get ItemStacks, not all types of ingredients.
	 *
	 * @see	IIngredientManager#getAllIngredients(IIngredientType)
	 * to get all the ingredients known to JEI, not just ones currently shown by the filter
	 */
	<T> List<T> getFilteredIngredients(IIngredientType<T> ingredientType);
}
