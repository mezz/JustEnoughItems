package mezz.jei.recipes.crafting;

import mezz.jei.api.recipes.IRecipeGui;
import mezz.jei.api.recipes.IRecipeHelper;
import mezz.jei.api.recipes.IRecipeType;
import mezz.jei.api.recipes.RecipeType;
import mezz.jei.util.StackUtil;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import java.util.ArrayList;
import java.util.List;

public class ShapelessOreRecipeHelper implements IRecipeHelper {

	@Override
	public IRecipeType getRecipeType() {
		return RecipeType.CRAFTING_TABLE;
	}

	@Override
	public Class getRecipeClass() {
		return ShapelessOreRecipe.class;
	}

	@Override
	public IRecipeGui createGui() {
		return new ShapelessOreRecipeGui();
	}

	@Override
	public List<ItemStack> getInputs(Object recipe) {
		ShapelessOreRecipe shapelessRecipe = (ShapelessOreRecipe)recipe;
		return StackUtil.getItemStacksRecursive(shapelessRecipe.getInput());
	}

	@Override
	public List<ItemStack> getOutputs(Object recipe) {
		ShapelessOreRecipe shapelessRecipe = (ShapelessOreRecipe)recipe;
		ArrayList<ItemStack> list = new ArrayList<ItemStack>();
		list.add(shapelessRecipe.getRecipeOutput());
		return list;
	}
}
