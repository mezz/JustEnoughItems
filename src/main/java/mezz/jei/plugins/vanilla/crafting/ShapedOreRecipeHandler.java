package mezz.jei.plugins.vanilla.crafting;

import javax.annotation.Nonnull;
import java.util.List;

import mezz.jei.util.ErrorUtil;
import mezz.jei.util.Log;
import net.minecraftforge.oredict.ShapedOreRecipe;

import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;

public class ShapedOreRecipeHandler implements IRecipeHandler<ShapedOreRecipe> {

	@Override
	@Nonnull
	public Class<ShapedOreRecipe> getRecipeClass() {
		return ShapedOreRecipe.class;
	}

	@Nonnull
	@Override
	public String getRecipeCategoryUid() {
		return VanillaRecipeCategoryUid.CRAFTING;
	}

	@Override
	@Nonnull
	public IRecipeWrapper getRecipeWrapper(@Nonnull ShapedOreRecipe recipe) {
		return new ShapedOreRecipeWrapper(recipe);
	}

	@Override
	public boolean isRecipeValid(@Nonnull ShapedOreRecipe recipe) {
		if (recipe.getRecipeOutput() == null) {
			String recipeInfo = ErrorUtil.getInfoFromBrokenRecipe(recipe, this);
			Log.error("Recipe has no output. {}", recipeInfo);
			return false;
		}
		int inputCount = 0;
		for (Object input : recipe.getInput()) {
			if (input instanceof List) {
				if (((List) input).size() == 0) {
					String recipeInfo = ErrorUtil.getInfoFromBrokenRecipe(recipe, this);
					Log.error("Recipe has an empty list as an input. {}", recipeInfo);
					return false;
				}
			}
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
