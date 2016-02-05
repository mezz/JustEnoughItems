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
	public FluidStack getMatch(Iterable<FluidStack> ingredients, @Nonnull Focus toMatch) {
		if (toMatch.getFluid() == null) {
			return null;
		}
		for (FluidStack fluidStack : ingredients) {
			if (toMatch.getFluid() == fluidStack.getFluid()) {
				return fluidStack;
			}
		}
		return null;
	}

	@Nonnull
	@Override
	public Focus createFocus(@Nonnull FluidStack ingredient) {
		return new Focus(ingredient.getFluid());
	}
}
