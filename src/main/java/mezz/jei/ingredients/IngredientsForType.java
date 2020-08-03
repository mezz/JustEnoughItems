package mezz.jei.ingredients;

import mezz.jei.api.ingredients.IIngredientType;

import java.util.List;

public class IngredientsForType<T> {
	private final IIngredientType<T> ingredientType;
	private List<List<T>> ingredients;

	public IngredientsForType(IIngredientType<T> ingredientType, List<List<T>> ingredients) {
		this.ingredientType = ingredientType;
		this.ingredients = ingredients;
	}

	public IIngredientType<T> getIngredientType() {
		return ingredientType;
	}

	public List<List<T>> getIngredients() {
		return ingredients;
	}

	public void setIngredients(List<List<T>> ingredients) {
		this.ingredients = ingredients;
	}
}
