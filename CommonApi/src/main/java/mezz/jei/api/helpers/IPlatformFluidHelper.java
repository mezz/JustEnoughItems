package mezz.jei.api.helpers;

import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

/**
 * Helper for mods that want to handle Fluid ingredients across multiple platforms (Forge and Fabric).
 * @param <T> the type of Fluid ingredient for the current platform.
 * @since 10.1.0
 */
public interface IPlatformFluidHelper<T> {
    /**
     * Returns the type of Fluid ingredients on the current platform.
     * @since 10.1.0
     */
    IIngredientTypeWithSubtypes<Fluid, T> getFluidIngredientType();

    /**
     * Creates a new fluid ingredient for the current platform.
     * @since 10.1.0
     */
    T create(Fluid fluid, long amount, @Nullable CompoundTag tag);

    /**
     * Creates a new fluid ingredient for the current platform.
     * @since 10.1.0
     */
    T create(Fluid fluid, long amount);

    /**
     * Returns amount of Fluid in one bucket on the current platform.
     * @since 10.1.0
     */
    long bucketVolume();
}
