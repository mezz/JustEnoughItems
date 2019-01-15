package mezz.jei.gui.ingredients;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.config.IIngredientFilterConfig;

public interface IIngredientListElement<V> {
	V getIngredient();

	int getOrderIndex();

	IIngredientHelper<V> getIngredientHelper();

	IIngredientRenderer<V> getIngredientRenderer();

	String getDisplayName();

	String getModNameForSorting();

	Set<String> getModNameStrings();

	List<String> getTooltipStrings(IIngredientFilterConfig config);

	Collection<String> getTagStrings();

	Collection<String> getCreativeTabsStrings();

	Collection<String> getColorStrings();

	String getResourceId();

	boolean isVisible();

	void setVisible(boolean visible);
}
