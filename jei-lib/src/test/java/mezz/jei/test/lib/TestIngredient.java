package mezz.jei.test.lib;

import mezz.jei.api.recipe.IIngredientType;

public class TestIngredient {
	public static final IIngredientType<TestIngredient> TYPE = () -> TestIngredient.class;

	private final int number;

	public TestIngredient(int number) {
		this.number = number;
	}

	public int getNumber() {
		return number;
	}

	public TestIngredient copy() {
		return new TestIngredient(number);
	}

	@Override
	public String toString() {
		return "TestIngredient#" + number;
	}
}
