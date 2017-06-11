package mezz.jei.plugins.vanilla.crafting;

import java.util.List;

import mezz.jei.Internal;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import mezz.jei.api.recipe.wrapper.IShapedCraftingRecipeWrapper;
import mezz.jei.recipes.BrokenCraftingRecipeException;
import mezz.jei.util.ErrorUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapedRecipes;

public class ShapedRecipesWrapper implements IShapedCraftingRecipeWrapper {

	private final ShapedRecipes recipe;

	public ShapedRecipesWrapper(ShapedRecipes recipe) {
		this.recipe = recipe;
	}

	@Override
	public void getIngredients(IIngredients ingredients) {
		List<List<ItemStack>> inputLists = Internal.getStackHelper().expandRecipeItemStackInputs(recipe.func_192400_c(), true);
		ItemStack recipeOutput = recipe.getRecipeOutput();
		try {
			ingredients.setInputLists(ItemStack.class, inputLists);
			ingredients.setOutput(ItemStack.class, recipeOutput);
		} catch (RuntimeException e) {
			String info = ErrorUtil.getInfoFromBrokenCraftingRecipe(recipe, inputLists, recipeOutput);
			throw new BrokenCraftingRecipeException(info, e);
		}
	}

	@Override
	public int getWidth() {
		return recipe.recipeWidth;
	}

	@Override
	public int getHeight() {
		return recipe.recipeHeight;
	}
}
