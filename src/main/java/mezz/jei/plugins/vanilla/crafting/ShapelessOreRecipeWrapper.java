package mezz.jei.plugins.vanilla.crafting;

import java.util.List;

import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import mezz.jei.api.recipe.IStackHelper;
import mezz.jei.api.recipe.wrapper.ICraftingRecipeWrapper;
import mezz.jei.util.BrokenCraftingRecipeException;
import mezz.jei.util.ErrorUtil;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapelessOreRecipe;

public class ShapelessOreRecipeWrapper extends BlankRecipeWrapper implements ICraftingRecipeWrapper {
	private final IJeiHelpers jeiHelpers;
	private final ShapelessOreRecipe recipe;

	public ShapelessOreRecipeWrapper(IJeiHelpers jeiHelpers, ShapelessOreRecipe recipe) {
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
		ItemStack recipeOutput = recipe.getRecipeOutput();

		try {
			List<List<ItemStack>> inputs = stackHelper.expandRecipeItemStackInputs(recipe.getInput());
			ingredients.setInputLists(ItemStack.class, inputs);

			if (recipeOutput != null) {
				ingredients.setOutput(ItemStack.class, recipeOutput);
			}
		} catch (RuntimeException e) {
			String info = ErrorUtil.getInfoFromBrokenCraftingRecipe(recipe, recipe.getInput(), recipeOutput);
			throw new BrokenCraftingRecipeException(info, e);
		}
	}
}
