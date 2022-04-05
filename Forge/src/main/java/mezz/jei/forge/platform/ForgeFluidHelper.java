package mezz.jei.forge.platform;

import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.common.platform.IPlatformFluidHelper;
import mezz.jei.forge.plugins.forge.ingredients.fluid.FluidStackRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

public class ForgeFluidHelper implements IPlatformFluidHelper<FluidStack> {
    @Override
    public IIngredientTypeWithSubtypes<Fluid, FluidStack> getFluidIngredientType() {
        return ForgeTypes.FLUID_STACK;
    }

    @Override
    public IIngredientSubtypeInterpreter<FluidStack> getAllNbtSubtypeInterpreter() {
        return AllFluidNbt.INSTANCE;
    }

    @Override
    public IIngredientRenderer<FluidStack> createRenderer(int capacityMb, boolean showCapacity, int width, int height) {
        return new FluidStackRenderer(capacityMb, showCapacity, width, height);
    }

    private static class AllFluidNbt implements IIngredientSubtypeInterpreter<FluidStack> {
        public static final AllFluidNbt INSTANCE = new AllFluidNbt();

        private AllFluidNbt() {
        }

        @Override
        public String apply(FluidStack fluidStack, UidContext context) {
            CompoundTag nbtTagCompound = fluidStack.getTag();
            if (nbtTagCompound == null || nbtTagCompound.isEmpty()) {
                return IIngredientSubtypeInterpreter.NONE;
            }
            return nbtTagCompound.toString();
        }
    }
}
