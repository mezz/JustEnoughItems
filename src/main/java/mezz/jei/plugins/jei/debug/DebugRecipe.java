package mezz.jei.plugins.jei.debug;

import java.util.Arrays;
import java.util.List;

import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import mezz.jei.api.recipe.BlankRecipeWrapper;

public class DebugRecipe extends BlankRecipeWrapper {
	public DebugRecipe() {

	}

	@Override
	public List<FluidStack> getFluidInputs() {
		return Arrays.asList(
				new FluidStack(FluidRegistry.WATER, 1000 + (int) (Math.random() * 1000)),
				new FluidStack(FluidRegistry.LAVA, 1000 + (int) (Math.random() * 1000))
		);
	}
}
