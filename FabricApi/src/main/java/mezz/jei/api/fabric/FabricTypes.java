package mezz.jei.api.fabric;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nonnull;

/**
 * Built-in {@link IIngredientType} for Fabric Minecraft.
 *
 * @since 10.1.0
 */
@SuppressWarnings("UnstableApiUsage")
public final class FabricTypes {
    /**
     * @since 10.1.0
     */
    @ApiStatus.Experimental
    public static final IIngredientTypeWithSubtypes<Fluid, StorageView<FluidVariant>> FLUID_STORAGE = new IIngredientTypeWithSubtypes<>() {
        @SuppressWarnings({"RedundantCast", "unchecked"})
        @Override
        @Nonnull
        public Class<? extends StorageView<FluidVariant>> getIngredientClass() {
            return (Class<? extends StorageView<FluidVariant>>) (Object) StorageView.class;
        }

        @Override
        @Nonnull
        public Class<? extends Fluid> getIngredientBaseClass() {
            return Fluid.class;
        }

        @Override
        @Nonnull
        public Fluid getBase(StorageView<FluidVariant> ingredient) {
            FluidVariant resource = ingredient.getResource();
            return resource.getFluid();
        }
    };

    private FabricTypes() {}
}
