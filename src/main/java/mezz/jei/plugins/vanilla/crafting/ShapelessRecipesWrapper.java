package mezz.jei.plugins.vanilla.crafting;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import mezz.jei.api.recipe.wrapper.ICraftingRecipeWrapper;
import mezz.jei.recipes.BrokenCraftingRecipeException;
import mezz.jei.util.ErrorUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapelessRecipes;

public class ShapelessRecipesWrapper extends BlankRecipeWrapper implements ICraftingRecipeWrapper {

	private final ShapelessRecipes recipe;

	public ShapelessRecipesWrapper(ShapelessRecipes recipe) {
		this.recipe = recipe;
	}

	@Override
	public void getIngredients(IIngredients ingredients) {
		ItemStack recipeOutput = recipe.getRecipeOutput();

		try {
			ingredients.setInputs(ItemStack.class, recipe.recipeItems);
			ingredients.setOutput(ItemStack.class, recipeOutput);
		} catch (RuntimeException e) {
			String info = ErrorUtil.getInfoFromBrokenCraftingRecipe(recipe, recipe.recipeItems, recipeOutput);
			throw new BrokenCraftingRecipeException(info, e);
		}
	}
}
