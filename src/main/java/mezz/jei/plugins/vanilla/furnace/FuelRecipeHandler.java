package mezz.jei.plugins.vanilla.furnace;

import javax.annotation.Nonnull;

import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.util.ErrorUtil;
import mezz.jei.util.Log;

public class FuelRecipeHandler implements IRecipeHandler<FuelRecipe> {
	@Override
	@Nonnull
	public Class<FuelRecipe> getRecipeClass() {
		return FuelRecipe.class;
	}

	@Nonnull
	@Override
	public String getRecipeCategoryUid() {
		return VanillaRecipeCategoryUid.FUEL;
	}

	@Nonnull
	@Override
	public String getRecipeCategoryUid(@Nonnull FuelRecipe recipe) {
		return VanillaRecipeCategoryUid.FUEL;
	}

	@Override
	@Nonnull
	public IRecipeWrapper getRecipeWrapper(@Nonnull FuelRecipe recipe) {
		return recipe;
	}

	@Override
	public boolean isRecipeValid(@Nonnull FuelRecipe recipe) {
		if (recipe.getInputs().isEmpty()) {
			String recipeInfo = ErrorUtil.getInfoFromBrokenRecipe(recipe, this);
			Log.error("Recipe has no inputs. {}", recipeInfo);
		}
		if (!recipe.getOutputs().isEmpty()) {
			String recipeInfo = ErrorUtil.getInfoFromBrokenRecipe(recipe, this);
			Log.error("Fuel Recipe should not have outputs. {}", recipeInfo);
		}
		return true;
	}
}
