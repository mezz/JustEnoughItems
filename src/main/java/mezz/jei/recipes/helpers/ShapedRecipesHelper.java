package mezz.jei.recipes.helpers;

import mezz.jei.api.recipes.IRecipeGui;
import mezz.jei.api.recipes.IRecipeHelper;
import mezz.jei.api.recipes.IRecipeType;
import mezz.jei.api.recipes.RecipeType;
import mezz.jei.gui.recipes.ShapedRecipesGui;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapedRecipes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ShapedRecipesHelper implements IRecipeHelper {

	@Override
	public IRecipeType getRecipeType() {
		return RecipeType.CRAFTING_TABLE;
	}

	@Override
	public Class getRecipeClass() {
		return ShapedRecipes.class;
	}

	@Override
	public IRecipeGui createGui() {
		return new ShapedRecipesGui();
	}

	@Override
	public List<ItemStack> getInputs(Object recipe) {
		ShapedRecipes shapedRecipe = (ShapedRecipes)recipe;
		ArrayList<ItemStack> list = new ArrayList<ItemStack>();
		Collections.addAll(list, shapedRecipe.recipeItems);
		return list;
	}

	@Override
	public List<ItemStack> getOutputs(Object recipe) {
		ShapedRecipes shapedRecipe = (ShapedRecipes)recipe;
		ArrayList<ItemStack> list = new ArrayList<ItemStack>();
		list.add(shapedRecipe.getRecipeOutput());
		return list;
	}
}
