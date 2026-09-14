package mezz.jei.common;

import mezz.jei.api.constants.ModIds;
import mezz.jei.api.recipe.types.IRecipeType;

public final class Constants {
	public static final IRecipeType<?> UNIVERSAL_RECIPE_TRANSFER_TYPE = IRecipeType.create(ModIds.JEI_ID, "universal_recipe_transfer_handler", Object.class);
	private Constants() {

	}
}
