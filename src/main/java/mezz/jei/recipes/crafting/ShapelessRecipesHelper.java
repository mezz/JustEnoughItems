package mezz.jei.recipes.crafting;

import mezz.jei.api.recipes.IRecipeGui;
import mezz.jei.api.recipes.IRecipeHelper;
import mezz.jei.api.recipes.IRecipeType;
import mezz.jei.api.recipes.RecipeType;
import mezz.jei.util.StackUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapelessRecipes;

import java.util.ArrayList;
import java.util.List;

public class ShapelessRecipesHelper implements IRecipeHelper {

	@Override
	public IRecipeType getRecipeType() {
		return RecipeType.CRAFTING_TABLE;
	}

	@Override
	public Class getRecipeClass() {
		return ShapelessRecipes.class;
	}

	@Override
	public IRecipeGui createGui() {
		return new ShapelessRecipesGui();
	}

	@Override
	public List<ItemStack> getInputs(Object recipe) {
		ShapelessRecipes shapelessRecipe = (ShapelessRecipes)recipe;
		return StackUtil.getItemStacksRecursive(shapelessRecipe.recipeItems);
	}

	@Override
	public List<ItemStack> getOutputs(Object recipe) {
		ShapelessRecipes shapelessRecipe = (ShapelessRecipes)recipe;
		ArrayList<ItemStack> list = new ArrayList<ItemStack>();
		list.add(shapelessRecipe.getRecipeOutput());
		return list;
	}
}
