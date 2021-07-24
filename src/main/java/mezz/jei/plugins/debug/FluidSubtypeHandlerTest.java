package mezz.jei.plugins.debug;

import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

public class FluidSubtypeHandlerTest implements IIngredientSubtypeInterpreter<FluidStack> {

	@Override
	public String apply(FluidStack fluidStack, UidContext context) {
		ResourceLocation name = fluidStack.getFluid().getRegistryName();
		if (name != null) {
			return fluidStack.getFluid().getRegistryName().toString();
		} else {
			throw new IllegalArgumentException("Fluid has no registry name! " + fluidStack.getFluid().toString());
		}
	}
}
