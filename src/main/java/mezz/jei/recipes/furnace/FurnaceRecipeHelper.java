package mezz.jei.recipes.furnace;

import mezz.jei.api.recipes.IRecipeGui;
import mezz.jei.api.recipes.IRecipeHelper;
import mezz.jei.api.recipes.IRecipeType;
import mezz.jei.api.recipes.RecipeType;
import net.minecraft.item.ItemStack;

import java.util.Arrays;
import java.util.List;

public class FurnaceRecipeHelper implements IRecipeHelper {
	@Override
	public Class getRecipeClass() {
		return FurnaceRecipe.class;
	}

	@Override
	public IRecipeType getRecipeType() {
		return RecipeType.FURNACE;
	}

	@Override
	public IRecipeGui createGui() {
		return new FurnaceRecipeGui();
	}

	@Override
	public List<ItemStack> getInputs(Object recipe) {
		FurnaceRecipe furnaceRecipe = (FurnaceRecipe)recipe;
		return furnaceRecipe.getInput();
	}

	@Override
	public List<ItemStack> getOutputs(Object recipe) {
		FurnaceRecipe furnaceRecipe = (FurnaceRecipe)recipe;
		return Arrays.asList(furnaceRecipe.getOutput());
	}
}
