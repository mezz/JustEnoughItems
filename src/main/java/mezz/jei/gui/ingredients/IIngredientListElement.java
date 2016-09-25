package mezz.jei.gui.ingredients;

import mezz.jei.api.ingredients.IIngredientHelper;

public interface IIngredientListElement<V> {
	V getIngredient();

	IIngredientHelper<V> getIngredientHelper();

	String getSearchString();

	String getModNameString();

	String getTooltipString();

	String getOreDictString();

	String getCreativeTabsString();

	String getColorString();
}
