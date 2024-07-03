package mezz.jei.library.plugins.debug.ingredients;

import mezz.jei.api.ingredients.IIngredientType;

public class DebugIngredient {
	public static final IIngredientType<DebugIngredient> TYPE = new IIngredientType<>() {
		@Override
		public String getUid() {
			return "debug";
		}

		@Override
		public Class<? extends DebugIngredient> getIngredientClass() {
			return DebugIngredient.class;
		}
	};

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
