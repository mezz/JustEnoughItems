package mezz.jei.plugins.vanilla.crafting;

import java.util.Arrays;
import java.util.List;

import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import mezz.jei.api.recipe.IStackHelper;
import mezz.jei.api.recipe.wrapper.IShapedCraftingRecipeWrapper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.oredict.ShapedOreRecipe;

public class ShapedOreRecipeWrapper extends BlankRecipeWrapper implements IShapedCraftingRecipeWrapper {
	private final IJeiHelpers jeiHelpers;
	private final ShapedOreRecipe recipe;

	public ShapedOreRecipeWrapper(IJeiHelpers jeiHelpers, ShapedOreRecipe recipe) {
		this.jeiHelpers = jeiHelpers;
		this.recipe = recipe;
		for (Object input : this.recipe.getInput()) {
			if (input instanceof ItemStack) {
				ItemStack itemStack = (ItemStack) input;
				if (itemStack.getCount() != 1) {
					itemStack.setCount(1);
				}
			}
		}
	}

	@Override
	public void getIngredients(IIngredients ingredients) {
		IStackHelper stackHelper = jeiHelpers.getStackHelper();

		List<List<ItemStack>> inputs = stackHelper.expandRecipeItemStackInputs(Arrays.asList(recipe.getInput()));
		ingredients.setInputLists(ItemStack.class, inputs);

		ItemStack recipeOutput = recipe.getRecipeOutput();
		if (recipeOutput != null) {
			ingredients.setOutput(ItemStack.class, recipeOutput);
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
