package mezz.jei.gui.ingredients;

import java.util.Collection;
import java.util.List;

import mezz.jei.api.ingredients.IIngredientHelper;

public interface IIngredientListElement<V> {
	V getIngredient();

	IIngredientHelper<V> getIngredientHelper();

	String getDisplayName();

	String getModName();

	String getModId();

	List<String> getTooltipStrings();

	Collection<String> getOreDictStrings();

	Collection<String> getCreativeTabsStrings();

	Collection<String> getColorStrings();

	String getResourceId();
}
