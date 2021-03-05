package mezz.jei.plugins.debug;

import mezz.jei.api.ingredients.subtypes.IFluidSubtypeInterpreter;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

public class FluidSubtypeHandlerTest implements IFluidSubtypeInterpreter {

    @Override
    public String apply(FluidStack fluidStack) {
        ResourceLocation name = fluidStack.getFluid().getRegistryName();
        if (name != null)
            return fluidStack.getFluid().getRegistryName().toString();
        else throw new IllegalArgumentException("Fluid has no registry name! " + fluidStack.getFluid().toString());
    }
}
