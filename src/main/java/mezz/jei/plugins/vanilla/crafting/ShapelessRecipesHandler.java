package mezz.jei.plugins.vanilla.crafting;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.util.ErrorUtil;
import mezz.jei.util.Log;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapelessRecipes;

public class ShapelessRecipesHandler implements IRecipeHandler<ShapelessRecipes> {
	private final IGuiHelper guiHelper;

	public ShapelessRecipesHandler(IGuiHelper guiHelper) {
		this.guiHelper = guiHelper;
	}

	@Override
	public Class<ShapelessRecipes> getRecipeClass() {
		return ShapelessRecipes.class;
	}

	@Override
	public String getRecipeCategoryUid() {
		return VanillaRecipeCategoryUid.CRAFTING;
	}

	@Override
	public String getRecipeCategoryUid(ShapelessRecipes recipe) {
		return VanillaRecipeCategoryUid.CRAFTING;
	}

	@Override
	public IRecipeWrapper getRecipeWrapper(ShapelessRecipes recipe) {
		return new ShapelessRecipesWrapper(guiHelper, recipe);
	}

	@Override
	public boolean isRecipeValid(ShapelessRecipes recipe) {
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
