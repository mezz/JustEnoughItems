package mezz.jei.gui.ingredients;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.ISortableIngredient;

public interface IIngredientListElement<T> extends ISortableIngredient<T> {

	IIngredientHelper<T> getIngredientHelper();

	IIngredientRenderer<T> getIngredientRenderer();

	Set<String> getModNameStrings();

	List<String> getTooltipStrings();

	Collection<String> getOreDictStrings();

	Collection<String> getCreativeTabsStrings();

	Collection<String> getColorStrings();

	String getResourceId();

	boolean isVisible();

	void setVisible(boolean visible);
}
