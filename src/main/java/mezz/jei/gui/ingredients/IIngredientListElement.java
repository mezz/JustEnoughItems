package mezz.jei.gui.ingredients;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;

public interface IIngredientListElement<V> {
	V getIngredient();

	int getOrderIndex();

	IIngredientHelper<V> getIngredientHelper();

	IIngredientRenderer<V> getIngredientRenderer();

	String getDisplayName();

	String getModNameForSorting();

	Set<String> getModNameStrings();

	List<String> getTooltipStrings();

	Collection<String> getOreDictStrings();

	Collection<String> getCreativeTabsStrings();

	Collection<String> getColorStrings();

	String getResourceId();

	boolean isVisible();

	void setVisible(boolean visible);
}
