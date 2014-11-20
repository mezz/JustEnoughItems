package mezz.jei.recipes.furnace.fuel;

import mezz.jei.api.recipes.IRecipeGui;
import mezz.jei.api.recipes.IRecipeHelper;
import mezz.jei.api.recipes.IRecipeType;
import mezz.jei.api.recipes.RecipeType;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class FuelRecipeHelper implements IRecipeHelper {
	@Override
	public Class getRecipeClass() {
		return FuelRecipe.class;
	}

	@Override
	public IRecipeType getRecipeType() {
		return RecipeType.FURNACE;
	}

	@Override
	public IRecipeGui createGui() {
		return new FuelRecipeGui();
	}

	@Override
	public List<ItemStack> getInputs(Object recipe) {
		FuelRecipe fuelRecipe = (FuelRecipe)recipe;
		return fuelRecipe.getInput();
	}

	@Override
	public List<ItemStack> getOutputs(Object recipe) {
		return new ArrayList<ItemStack>(0);
	}
}
