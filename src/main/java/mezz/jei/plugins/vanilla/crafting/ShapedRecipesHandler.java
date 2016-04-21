package mezz.jei.plugins.vanilla.crafting;

import javax.annotation.Nonnull;

import mezz.jei.util.ErrorUtil;
import mezz.jei.util.Log;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapedRecipes;

import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;

public class ShapedRecipesHandler implements IRecipeHandler<ShapedRecipes> {

	@Override
	@Nonnull
	public Class<ShapedRecipes> getRecipeClass() {
		return ShapedRecipes.class;
	}

	@Nonnull
	@Override
	public String getRecipeCategoryUid() {
		return VanillaRecipeCategoryUid.CRAFTING;
	}

	@Override
	@Nonnull
	public IRecipeWrapper getRecipeWrapper(@Nonnull ShapedRecipes recipe) {
		return new ShapedRecipesWrapper(recipe);
	}

	@Override
	public boolean isRecipeValid(@Nonnull ShapedRecipes recipe) {
		if (recipe.getRecipeOutput() == null) {
			String recipeInfo = ErrorUtil.getInfoFromBrokenRecipe(recipe, this);
			Log.error("Recipe has no outputs. {}", recipeInfo);
			return false;
		}
		int inputCount = 0;
		for (ItemStack input : recipe.recipeItems) {
			if (input != null) {
				inputCount++;
			}
		}
		if (inputCount > 9) {
			String recipeInfo = ErrorUtil.getInfoFromBrokenRecipe(recipe, this);
			Log.error("Recipe has too many inputs. {}", recipeInfo);
			return false;
		}
		if (inputCount == 0) {
			String recipeInfo = ErrorUtil.getInfoFromBrokenRecipe(recipe, this);
			Log.error("Recipe has no inputs. {}", recipeInfo);
			return false;
		}
		return true;
	}
}
