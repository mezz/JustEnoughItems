package mezz.jei.api.config;

import java.util.List;

public enum IngredientSortStage {
	MOD_NAME, INGREDIENT_TYPE, ALPHABETICAL, CREATIVE_MENU, TAG, ARMOR, MAX_DURABILITY;

	public static final List<IngredientSortStage> defaultStages = List.of(
		IngredientSortStage.MOD_NAME,
		IngredientSortStage.INGREDIENT_TYPE,
		IngredientSortStage.CREATIVE_MENU
	);

	public static boolean isValidName(String name) {
		try {
			valueOf(name);
			return true;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}
}
