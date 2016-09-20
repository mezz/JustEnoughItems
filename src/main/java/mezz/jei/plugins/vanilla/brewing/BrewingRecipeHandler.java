package mezz.jei.plugins.vanilla.brewing;

import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.util.ErrorUtil;
import mezz.jei.util.Log;

public class BrewingRecipeHandler implements IRecipeHandler<BrewingRecipeWrapper> {
	@Override
	public Class<BrewingRecipeWrapper> getRecipeClass() {
		return BrewingRecipeWrapper.class;
	}

	@Override
	public String getRecipeCategoryUid() {
		return VanillaRecipeCategoryUid.BREWING;
	}

	@Override
	public String getRecipeCategoryUid(BrewingRecipeWrapper recipe) {
		return VanillaRecipeCategoryUid.BREWING;
	}

	@Override
	public BrewingRecipeWrapper getRecipeWrapper(BrewingRecipeWrapper recipe) {
		return recipe;
	}

	@Override
	public boolean isRecipeValid(BrewingRecipeWrapper recipe) {
		if (recipe.getInputs().size() != 4) {
			String recipeInfo = ErrorUtil.getInfoFromBrokenRecipe(recipe, this);
			Log.error("Recipe has the wrong number of inputs (needs 4). {}", recipeInfo);
			return false;
		}
		if (recipe.getOutputs().size() != 1) {
			String recipeInfo = ErrorUtil.getInfoFromBrokenRecipe(recipe, this);
			Log.error("Recipe has the wrong number of outputs (needs 1). {}", recipeInfo);
			return false;
		}
		return true;
	}
}
