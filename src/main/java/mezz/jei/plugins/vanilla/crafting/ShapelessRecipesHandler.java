package mezz.jei.plugins.vanilla.crafting;

import javax.annotation.Nonnull;

import mezz.jei.util.ErrorUtil;
import mezz.jei.util.Log;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapelessRecipes;

import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;

public class ShapelessRecipesHandler implements IRecipeHandler<ShapelessRecipes> {

	@Override
	@Nonnull
	public Class<ShapelessRecipes> getRecipeClass() {
		return ShapelessRecipes.class;
	}

	@Nonnull
	@Override
	public String getRecipeCategoryUid() {
		return VanillaRecipeCategoryUid.CRAFTING;
	}

	@Override
	@Nonnull
	public IRecipeWrapper getRecipeWrapper(@Nonnull ShapelessRecipes recipe) {
		return new ShapelessRecipesWrapper(recipe);
	}

	@Override
	public boolean isRecipeValid(@Nonnull ShapelessRecipes recipe) {
		if (recipe.getRecipeOutput() == null) {
			String recipeInfo = ErrorUtil.getInfoFromBrokenRecipe(recipe, this);
			Log.error("Recipe has no output. {}", recipeInfo);
			return false;
		}
		int inputCount = 0;
		for (Object input : recipe.recipeItems) {
			if (input instanceof ItemStack) {
				inputCount++;
			} else {
				String recipeInfo = ErrorUtil.getInfoFromBrokenRecipe(recipe, this);
				Log.error("Recipe has an input that is not an ItemStack. {}", recipeInfo);
				return false;
			}
		}
		if (inputCount > 9) {
			String recipeInfo = ErrorUtil.getInfoFromBrokenRecipe(recipe, this);
			Log.error("Recipe has too many inputs. {}", recipeInfo);
			return false;
		}
		return inputCount > 0;
	}
}
