package mezz.jei.api.ingredients;

import java.util.List;

import mezz.jei.api.IJeiHelpers;
import mezz.jei.gui.ingredients.IIngredientListElement;

/**
 * The Ingredient bookmaks allows mods add and remove ingredients from JEI's
 * bookmark list
 * Get the instance from {@link IJeiHelpers#getIngredientBookmarks()}.
 *
 * @Since JEI 3.14.9
 */
public interface IIngredientBookmarks {
	/**
	 * Toggles visibility of ingredient in the bookmark list.
	 */
	<V> void toggleIngredientBookmark(V ingredient);

	List<IIngredientListElement> getIngredientList();

	void clear();
}
