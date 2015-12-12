package mezz.jei.api.recipe;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;

import net.minecraftforge.fluids.FluidStack;

public abstract class BlankRecipeWrapper implements IRecipeWrapper {
	@Override
	public List getInputs() {
		return Collections.emptyList();
	}

	@Override
	public List getOutputs() {
		return Collections.emptyList();
	}

	@Override
	public List<FluidStack> getFluidInputs() {
		return Collections.emptyList();
	}

	@Override
	public List<FluidStack> getFluidOutputs() {
		return Collections.emptyList();
	}

	@Override
	public void drawInfo(@Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight) {

	}

	@Override
	public void drawAnimations(@Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight) {

	}
}
