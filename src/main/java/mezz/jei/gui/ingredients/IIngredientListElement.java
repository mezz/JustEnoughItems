package mezz.jei.gui.ingredients;

import mezz.jei.api.ingredients.IIngredientHelper;

public interface IIngredientListElement<V> {
	V getIngredient();

	IIngredientHelper<V> getIngredientHelper();

	String getDisplayName();

	String getModName();

	String getModId();

	String getTooltipString();

	String getOreDictString();

	String getCreativeTabsString();

	String getColorString();

	String getResourceId();
}
