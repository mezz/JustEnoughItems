package mezz.jei.api.forge;

import mezz.jei.api.ingredients.IIngredientType;
import net.minecraftforge.fluids.FluidStack;

/**
 * Built-in {@link IIngredientType} for Forge Minecraft.
 *
 * @since 9.6.0
 */
public final class ForgeTypes {
    /**
     * @since 9.6.0
     */
    public static final IIngredientType<FluidStack> FLUID = () -> FluidStack.class;

    private ForgeTypes() {}
}
