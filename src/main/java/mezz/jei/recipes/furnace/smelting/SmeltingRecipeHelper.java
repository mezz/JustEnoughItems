package mezz.jei.recipes.furnace.smelting;

import mezz.jei.api.recipes.IRecipeGui;
import mezz.jei.api.recipes.IRecipeHelper;
import mezz.jei.api.recipes.IRecipeType;
import mezz.jei.api.recipes.RecipeType;
import net.minecraft.item.ItemStack;

import java.util.Arrays;
import java.util.List;

public class SmeltingRecipeHelper implements IRecipeHelper {
	@Override
	public Class getRecipeClass() {
		return SmeltingRecipe.class;
	}

	@Override
	public IRecipeType getRecipeType() {
		return RecipeType.FURNACE;
	}

	@Override
	public IRecipeGui createGui() {
		return new SmeltingRecipeGui();
	}

	@Override
	public List<ItemStack> getInputs(Object recipe) {
		SmeltingRecipe smeltingRecipe = (SmeltingRecipe)recipe;
		return smeltingRecipe.getInput();
	}

	@Override
	public List<ItemStack> getOutputs(Object recipe) {
		SmeltingRecipe smeltingRecipe = (SmeltingRecipe)recipe;
		return Arrays.asList(smeltingRecipe.getOutput());
	}
}
