package mezz.jei.plugins.vanilla.crafting;

import java.util.List;

import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IStackHelper;
import mezz.jei.api.recipe.wrapper.ICraftingRecipeWrapper;
import mezz.jei.recipes.BrokenCraftingRecipeException;
import mezz.jei.util.ErrorUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;

public class ShapelessRecipeWrapper<T extends IRecipe> implements ICraftingRecipeWrapper {
	private final IJeiHelpers jeiHelpers;
	protected final T recipe;

	public ShapelessRecipeWrapper(IJeiHelpers jeiHelpers, T recipe) {
		this.jeiHelpers = jeiHelpers;
		this.recipe = recipe;
	}

	@Override
	public void getIngredients(IIngredients ingredients) {
		ItemStack recipeOutput = recipe.getRecipeOutput();
		IStackHelper stackHelper = jeiHelpers.getStackHelper();

		try {
			List<List<ItemStack>> inputLists = stackHelper.expandRecipeItemStackInputs(recipe.func_192400_c());
			ingredients.setInputLists(ItemStack.class, inputLists);
			ingredients.setOutput(ItemStack.class, recipeOutput);
		} catch (RuntimeException e) {
			String info = ErrorUtil.getInfoFromBrokenCraftingRecipe(recipe, recipe.func_192400_c(), recipeOutput);
			throw new BrokenCraftingRecipeException(info, e);
		}
	}
}
