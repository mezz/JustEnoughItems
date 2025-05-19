package mezz.jei.library.load.registration;

import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.advanced.IRecipeManagerPluginHelper;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.library.recipes.RecipeManagerInternal;

public class RecipeManagerPluginHelper implements IRecipeManagerPluginHelper {
	private final RecipeManagerInternal recipeManager;

	public RecipeManagerPluginHelper(RecipeManagerInternal recipeManager) {
		this.recipeManager = recipeManager;
	}

	@Override
	public boolean isCraftingStation(IRecipeType<?> recipeType, IFocus<?> focus) {
		return recipeManager.isCraftingStation(recipeType, focus);
	}
}
