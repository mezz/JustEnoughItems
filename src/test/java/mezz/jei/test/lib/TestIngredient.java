package mezz.jei.test.lib;

import javax.annotation.Nullable;

import mezz.jei.api.recipe.IIngredientType;

public class TestIngredient {
	public static final IIngredientType<TestIngredient> TYPE = () -> TestIngredient.class;

	private final int number;
	@Nullable
	private String displayName;

	public TestIngredient(int number) {
		this.number = number;
	}

	public int getNumber() {
		return number;
	}

	public TestIngredient copy() {
		TestIngredient copy = new TestIngredient(number);
		copy.displayName = displayName;
		return copy;
	}

	public String getDisplayName() {
		return displayName == null ? toString() : displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	@Override
	public String toString() {
		return "TestIngredient#" + number;
	}
}
