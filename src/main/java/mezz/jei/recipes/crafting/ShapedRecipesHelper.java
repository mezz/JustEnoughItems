package mezz.jei.recipes.crafting;

import mezz.jei.api.recipes.IRecipeGuiHelper;
import mezz.jei.api.recipes.IRecipeHelper;
import mezz.jei.api.recipes.IRecipeType;
import mezz.jei.api.recipes.RecipeType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapedRecipes;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ShapedRecipesHelper implements IRecipeHelper {

	@Nonnull
	@Override
	public IRecipeType getRecipeType() {
		return RecipeType.CRAFTING_TABLE;
	}

	@Nonnull
	@Override
	public Class getRecipeClass() {
		return ShapedRecipes.class;
	}

	@Nonnull
	@Override
	public IRecipeGuiHelper createGuiHelper() {
		return new ShapedRecipesGui();
	}

	@Nonnull
	@Override
	public List<ItemStack> getInputs(@Nonnull Object recipe) {
		ShapedRecipes shapedRecipe = (ShapedRecipes)recipe;
		ArrayList<ItemStack> list = new ArrayList<ItemStack>();
		Collections.addAll(list, shapedRecipe.recipeItems);
		return list;
	}

	@Nonnull
	@Override
	public List<ItemStack> getOutputs(@Nonnull Object recipe) {
		ShapedRecipes shapedRecipe = (ShapedRecipes)recipe;
		ArrayList<ItemStack> list = new ArrayList<ItemStack>();
		list.add(shapedRecipe.getRecipeOutput());
		return list;
	}
}
