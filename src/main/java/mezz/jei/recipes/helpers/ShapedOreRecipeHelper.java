package mezz.jei.recipes.helpers;

import mezz.jei.api.gui.IRecipeGui;
import mezz.jei.api.recipes.IRecipeHelper;
import mezz.jei.gui.recipes.ShapedOreRecipeGui;
import mezz.jei.util.Log;
import mezz.jei.util.StackUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraftforge.oredict.ShapedOreRecipe;
import scala.actors.threadpool.Arrays;

import java.util.ArrayList;
import java.util.List;

public class ShapedOreRecipeHelper implements IRecipeHelper
{
	@Override
	public String getTitle() {
		return StatCollector.translateToLocal("gui.jei.shapedOreRecipes");
	}

	@Override
	public Class getRecipeClass() {
		return ShapedOreRecipe.class;
	}

	@Override
	public IRecipeGui createGui() {
		return new ShapedOreRecipeGui();
	}

	@Override
	public List<ItemStack> getInputs(Object recipe) {
		ShapedOreRecipe shapedRecipe = (ShapedOreRecipe)recipe;
		List list = Arrays.asList(shapedRecipe.getInput());
		List<ItemStack> inputsList = StackUtil.getItemStacksRecursive(list);
		if (inputsList == null)
			Log.error("Error in recipe: " + recipe);
		return inputsList;
	}

	@Override
	public List<ItemStack> getOutputs(Object recipe) {
		ShapedOreRecipe shapedRecipe = (ShapedOreRecipe)recipe;
		ArrayList<ItemStack> list = new ArrayList<ItemStack>();
		list.add(shapedRecipe.getRecipeOutput());
		return list;
	}
}
