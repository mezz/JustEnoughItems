package mezz.jei.plugins.jei.ingredients;

import mezz.jei.api.recipe.IIngredientType;

public class DebugIngredient {
	public static final IIngredientType<DebugIngredient> TYPE = () -> DebugIngredient.class;

	private final int number;

	public DebugIngredient(int number) {
		this.number = number;
	}

	public int getNumber() {
		return number;
	}

	public DebugIngredient copy() {
		return new DebugIngredient(number);
	}
}
