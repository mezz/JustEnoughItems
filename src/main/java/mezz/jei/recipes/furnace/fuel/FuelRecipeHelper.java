package mezz.jei.recipes.furnace.fuel;

import mezz.jei.api.recipes.IRecipeGui;
import mezz.jei.api.recipes.IRecipeHelper;
import mezz.jei.api.recipes.IRecipeType;
import mezz.jei.api.recipes.RecipeType;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class FuelRecipeHelper implements IRecipeHelper {
	@Nonnull
	@Override
	public Class getRecipeClass() {
		return FuelRecipe.class;
	}

	@Nonnull
	@Override
	public IRecipeType getRecipeType() {
		return RecipeType.FURNACE;
	}

	@Nonnull
	@Override
	public IRecipeGui createGui() {
		return new FuelRecipeGui();
	}

	@Nonnull
	@Override
	public List<ItemStack> getInputs(@Nonnull Object recipe) {
		FuelRecipe fuelRecipe = (FuelRecipe)recipe;
		return fuelRecipe.getInput();
	}

	@Nonnull
	@Override
	public List<ItemStack> getOutputs(@Nonnull Object recipe) {
		return new ArrayList<ItemStack>(0);
	}
}
