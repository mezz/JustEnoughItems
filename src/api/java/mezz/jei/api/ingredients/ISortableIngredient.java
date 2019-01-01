package mezz.jei.api.ingredients;

import mezz.jei.api.recipe.IIngredientType;

public interface ISortableIngredient<T> {
	T getIngredient();

	IIngredientType<T> getIngredientType();

	int getCreativeMenuOrder();

	String getDisplayName();

	String getModName();
}
