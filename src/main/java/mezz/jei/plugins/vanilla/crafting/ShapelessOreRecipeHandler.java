package mezz.jei.plugins.vanilla.crafting;

import java.util.List;

import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.util.ErrorUtil;
import mezz.jei.util.Log;
import net.minecraftforge.oredict.ShapelessOreRecipe;

public class ShapelessOreRecipeHandler implements IRecipeHandler<ShapelessOreRecipe> {
	private final IJeiHelpers jeiHelpers;

	public ShapelessOreRecipeHandler(IJeiHelpers jeiHelpers) {
		this.jeiHelpers = jeiHelpers;
	}

	@Override
	public Class<ShapelessOreRecipe> getRecipeClass() {
		return ShapelessOreRecipe.class;
	}

	@Override
	public String getRecipeCategoryUid(ShapelessOreRecipe recipe) {
		return VanillaRecipeCategoryUid.CRAFTING;
	}

	@Override
	public IRecipeWrapper getRecipeWrapper(ShapelessOreRecipe recipe) {
		return new ShapelessOreRecipeWrapper(jeiHelpers, recipe);
	}

	@SuppressWarnings("ConstantConditions")
	@Override
	public boolean isRecipeValid(ShapelessOreRecipe recipe) {
		if (recipe.getRecipeOutput() == null) {
			String recipeInfo = ErrorUtil.getInfoFromRecipe(recipe, this);
			Log.error("Recipe has no outputs. {}", recipeInfo);
			return false;
		}
		int inputCount = 0;
		for (Object input : recipe.getInput()) {
			if (input instanceof List) {
				if (((List) input).isEmpty()) {
					// missing items for an oreDict name. This is normal behavior, but the recipe is invalid.
					return false;
				}
			}
			if (input != null) {
				inputCount++;
			}
		}
		if (inputCount > 9) {
			String recipeInfo = ErrorUtil.getInfoFromRecipe(recipe, this);
			Log.error("Recipe has too many inputs. {}", recipeInfo);
			return false;
		}
		if (inputCount == 0) {
			String recipeInfo = ErrorUtil.getInfoFromRecipe(recipe, this);
			Log.error("Recipe has no inputs. {}", recipeInfo);
			return false;
		}
		return true;
	}
}
