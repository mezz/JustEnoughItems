package mezz.jei.test.lib;

import mezz.jei.api.ingredients.IIngredientType;

public class TestIngredient {
	public static final IIngredientType<TestIngredient> TYPE = new IIngredientType<>() {
		@Override
		public String getUid() {
			return "test";
		}

		@Override
		public Class<? extends TestIngredient> getIngredientClass() {
			return TestIngredient.class;
		}
	};

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
