package mezz.jei.gui.ingredients;

import javax.annotation.Nonnull;
import java.util.Collection;

import net.minecraftforge.fluids.FluidStack;

import mezz.jei.gui.Focus;

public class FluidStackHelper implements IIngredientHelper<FluidStack> {
	@Override
	public Collection<FluidStack> expandSubtypes(Collection<FluidStack> contained) {
		return contained;
	}

	@Override
	public FluidStack getMatch(Iterable<FluidStack> contained, @Nonnull Focus toMatch) {
		if (toMatch.getFluid() == null) {
			return null;
		}
		for (FluidStack fluidStack : contained) {
			if (toMatch.getFluid() == fluidStack.getFluid()) {
				return fluidStack;
			}
		}
		return null;
	}
}
