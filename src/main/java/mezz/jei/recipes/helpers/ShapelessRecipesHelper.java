package mezz.jei.recipes.helpers;

import mezz.jei.api.gui.IRecipeGui;
import mezz.jei.api.recipes.IRecipeHelper;
import mezz.jei.gui.recipes.ShapelessRecipesGui;
import mezz.jei.util.StackUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.util.StatCollector;

import java.util.ArrayList;
import java.util.List;

public class ShapelessRecipesHelper implements IRecipeHelper {

	@Override
	public String getTitle() {
		return StatCollector.translateToLocal("gui.jei.shapelessRecipes");
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
