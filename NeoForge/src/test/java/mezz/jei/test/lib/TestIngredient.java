package mezz.jei.test.lib;

import com.mojang.serialization.Codec;
import mezz.jei.api.ingredients.IIngredientType;

public record TestIngredient(int number) {
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

	public static final Codec<TestIngredient> CODEC = Codec.INT.xmap(TestIngredient::new, TestIngredient::number);


	public TestIngredient copy() {
		return new TestIngredient(number);
	}

	@Override
	public String toString() {
		return "TestIngredient#" + number;
	}
}
