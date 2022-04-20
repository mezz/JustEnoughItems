package mezz.jei.api.fabric.constants;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import mezz.jei.api.fabric.ingredients.fluids.IJeiFluidIngredient;
import net.minecraft.world.level.material.Fluid;

/**
 * Built-in {@link IIngredientType} for Fabric Minecraft.
 *
 * @since 10.1.0
 */
public final class FabricTypes {
    /**
     * @since 10.1.0
     */
    public static final IIngredientTypeWithSubtypes<Fluid, IJeiFluidIngredient> FLUID_STACK = new IIngredientTypeWithSubtypes<>() {
        @Override
        public Class<? extends IJeiFluidIngredient> getIngredientClass() {
            return IJeiFluidIngredient.class;
        }

        @Override
        public Class<? extends Fluid> getIngredientBaseClass() {
            return Fluid.class;
        }

        @Override
        public Fluid getBase(IJeiFluidIngredient ingredient) {
            return ingredient.getFluid();
        }
    };

    private FabricTypes() {}
}
