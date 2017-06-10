package mezz.jei.plugins.vanilla.crafting;

import java.util.Arrays;
import java.util.List;

import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import mezz.jei.api.recipe.IStackHelper;
import mezz.jei.api.recipe.wrapper.IShapedCraftingRecipeWrapper;
import mezz.jei.recipes.BrokenCraftingRecipeException;
import mezz.jei.util.ErrorUtil;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapedOreRecipe;

public class ShapedOreRecipeWrapper extends BlankRecipeWrapper implements IShapedCraftingRecipeWrapper {
	private final IJeiHelpers jeiHelpers;
	private final ShapedOreRecipe recipe;

	public ShapedOreRecipeWrapper(IJeiHelpers jeiHelpers, ShapedOreRecipe recipe) {
		this.jeiHelpers = jeiHelpers;
		this.recipe = recipe;
	}

	@Override
	public void getIngredients(IIngredients ingredients) {
		IStackHelper stackHelper = jeiHelpers.getStackHelper();
		ItemStack recipeOutput = recipe.getRecipeOutput();

		try {
			List<List<ItemStack>> inputs = stackHelper.expandRecipeItemStackInputs(recipe.func_192400_c());
			ingredients.setInputLists(ItemStack.class, inputs);
			ingredients.setOutput(ItemStack.class, recipeOutput);
		} catch (RuntimeException e) {
			String info = ErrorUtil.getInfoFromBrokenCraftingRecipe(recipe, recipe.func_192400_c(), recipeOutput);
			throw new BrokenCraftingRecipeException(info, e);
		}
	}

	@Override
	public int getWidth() {
		return recipe.getWidth();
	}

	@Override
	public int getHeight() {
		return recipe.getHeight();
	}

}
