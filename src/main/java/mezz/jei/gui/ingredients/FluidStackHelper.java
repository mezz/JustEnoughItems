package mezz.jei.gui.ingredients;

import javax.annotation.Nullable;
import java.util.Collection;

import mezz.jei.api.recipe.IFocus;
import mezz.jei.gui.Focus;
import net.minecraftforge.fluids.FluidStack;

public class FluidStackHelper implements IIngredientHelper<FluidStack> {
	@Override
	public Collection<FluidStack> expandSubtypes(Collection<FluidStack> contained) {
		return contained;
	}

	@Override
	@Nullable
	public FluidStack getMatch(Iterable<FluidStack> ingredients, IFocus<FluidStack> toMatch) {
		if (toMatch.getValue() == null) {
			return null;
		}
		for (FluidStack fluidStack : ingredients) {
			if (toMatch.getValue().getFluid() == fluidStack.getFluid()) {
				return fluidStack;
			}
		}
		return null;
	}

	@Override
	public Focus<FluidStack> createFocus(FluidStack ingredient) {
		return new Focus<FluidStack>(ingredient);
	}
}
