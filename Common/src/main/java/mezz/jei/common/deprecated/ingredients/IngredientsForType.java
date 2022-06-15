package mezz.jei.common.deprecated.ingredients;

import mezz.jei.api.ingredients.IIngredientType;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Deprecated
public class IngredientsForType<T> {
	private final IIngredientType<T> ingredientType;
	private List<@Nullable List<@Nullable T>> ingredients;

	public IngredientsForType(IIngredientType<T> ingredientType, List<@Nullable List<@Nullable T>> ingredients) {
		this.ingredientType = ingredientType;
		this.ingredients = ingredients;
	}

	public IIngredientType<T> getIngredientType() {
		return ingredientType;
	}

	public List<@Nullable List<@Nullable T>> getIngredients() {
		return ingredients;
	}

	public void setIngredients(List<@Nullable List<@Nullable T>> ingredients) {
		this.ingredients = ingredients;
	}
}
