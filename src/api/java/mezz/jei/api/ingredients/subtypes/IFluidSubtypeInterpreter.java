package mezz.jei.api.ingredients.subtypes;

import net.minecraftforge.fluids.FluidStack;

import java.util.function.Function;

@FunctionalInterface
public interface IFluidSubtypeInterpreter extends Function<FluidStack, String> {
    String NONE = "";

    /**
     * Get the data from an itemStack that is relevant to telling subtypes apart.
     * This should account for nbt, and anything else that's relevant.
     * Return {@link #NONE} if there is no data used for subtypes.
     */
    @Override
    String apply(FluidStack fluidStack);

    /**
     * Get the data from an itemStack that is relevant to telling subtypes apart in the given context.
     * This should account for nbt, and anything else that's relevant.
     * Return {@link #NONE} if there is no data used for subtypes.
     * @since JEI x.x.x
     */
    default String apply(FluidStack fluidStack, UidContext context) {
        return apply(fluidStack);
    }
}
