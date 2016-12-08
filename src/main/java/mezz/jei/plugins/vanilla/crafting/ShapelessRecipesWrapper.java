package mezz.jei.plugins.vanilla.crafting;

import java.util.Collections;
import java.util.List;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import mezz.jei.api.recipe.wrapper.ICraftingRecipeWrapper;
import mezz.jei.util.BrokenCraftingRecipeException;
import mezz.jei.util.ErrorUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapelessRecipes;

public class ShapelessRecipesWrapper extends AbstractShapelessRecipeWrapper {

	private final ShapelessRecipes recipe;

	public ShapelessRecipesWrapper(IGuiHelper guiHelper, ShapelessRecipes recipe) {
		super(guiHelper);
		this.recipe = recipe;
		for (Object input : this.recipe.recipeItems) {
			if (input instanceof ItemStack) {
				ItemStack itemStack = (ItemStack) input;
				if (itemStack.stackSize != 1) {
					itemStack.stackSize = 1;
				}
			}
		}
	}

	@Override
	public void getIngredients(IIngredients ingredients) {
		ItemStack recipeOutput = recipe.getRecipeOutput();

		try {
			ingredients.setInputs(ItemStack.class, recipe.recipeItems);
			if (recipeOutput != null) {
				ingredients.setOutput(ItemStack.class, recipeOutput);
			}
		} catch (RuntimeException e) {
			String info = ErrorUtil.getInfoFromBrokenCraftingRecipe(recipe, recipe.recipeItems, recipeOutput);
			throw new BrokenCraftingRecipeException(info, e);
		}
	}

	@Override
	public List<ItemStack> getInputs() {
		return recipe.recipeItems;
	}

	@Override
	public List<ItemStack> getOutputs() {
		return Collections.singletonList(recipe.getRecipeOutput());
	}
}
