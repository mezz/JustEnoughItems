package mezz.jei.plugins.vanilla;

import java.util.Collections;
import java.util.List;

import net.minecraftforge.fluids.FluidStack;

import mezz.jei.api.recipe.IRecipeWrapper;

public abstract class VanillaRecipeWrapper implements IRecipeWrapper {
	@Override
	public List<FluidStack> getFluidInputs() {
		return Collections.emptyList();
	}

	@Override
	public List<FluidStack> getFluidOutputs() {
		return Collections.emptyList();
	}
}
