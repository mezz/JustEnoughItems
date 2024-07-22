package mezz.jei.library.plugins.debug.ingredients;

import mezz.jei.api.ingredients.IIngredientType;

public record DebugIngredient(int number) {
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

	public DebugIngredient copy() {
		return new DebugIngredient(number);
	}
}
