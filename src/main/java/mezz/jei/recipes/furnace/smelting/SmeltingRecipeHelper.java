package mezz.jei.recipes.furnace.smelting;

import mezz.jei.api.recipes.IRecipeGui;
import mezz.jei.api.recipes.IRecipeHelper;
import mezz.jei.api.recipes.IRecipeType;
import mezz.jei.api.recipes.RecipeType;
import net.minecraft.item.ItemStack;


import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public class SmeltingRecipeHelper implements IRecipeHelper {
	@Nonnull
	@Override
	public Class getRecipeClass() {
		return SmeltingRecipe.class;
	}

	@Nonnull
	@Override
	public IRecipeType getRecipeType() {
		return RecipeType.FURNACE;
	}

	@Nonnull
	@Override
	public IRecipeGui createGui() {
		return new SmeltingRecipeGui();
	}

	@Nonnull
	@Override
	public List<ItemStack> getInputs(@Nonnull Object recipe) {
		SmeltingRecipe smeltingRecipe = (SmeltingRecipe)recipe;
		return smeltingRecipe.getInput();
	}

	@Nonnull
	@Override
	public List<ItemStack> getOutputs(@Nonnull Object recipe) {
		SmeltingRecipe smeltingRecipe = (SmeltingRecipe)recipe;
		return Collections.singletonList(smeltingRecipe.getOutput());
	}
}
