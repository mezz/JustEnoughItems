package mezz.jei.plugins.vanilla.furnace;

import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.util.ErrorUtil;
import mezz.jei.util.Log;

public class FuelRecipeHandler implements IRecipeHandler<FuelRecipe> {
	@Override
	public Class<FuelRecipe> getRecipeClass() {
		return FuelRecipe.class;
	}

	@Override
	public String getRecipeCategoryUid() {
		return VanillaRecipeCategoryUid.FUEL;
	}

	@Override
	public String getRecipeCategoryUid(FuelRecipe recipe) {
		return VanillaRecipeCategoryUid.FUEL;
	}

	@Override
	public IRecipeWrapper getRecipeWrapper(FuelRecipe recipe) {
		return recipe;
	}

	@Override
	public boolean isRecipeValid(FuelRecipe recipe) {
		if (recipe.getInputs().isEmpty()) {
			String recipeInfo = ErrorUtil.getInfoFromRecipe(recipe, this);
			Log.error("Recipe has no inputs. {}", recipeInfo);
		}
		if (!recipe.getOutputs().isEmpty()) {
			String recipeInfo = ErrorUtil.getInfoFromRecipe(recipe, this);
			Log.error("Fuel Recipe should not have outputs. {}", recipeInfo);
		}
		return true;
	}
}
