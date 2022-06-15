package mezz.jei.api.fabric.ingredients.fluids;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluid;

import java.util.Optional;

/**
 * Built-in ingredient for representing Fluids in Fabric Minecraft.
 *
 * @since 10.1.0
 */
public interface IJeiFluidIngredient {
    /**
     * @return the fluid represented by this ingredient.
     *
     * @since 10.1.0
     */
    Fluid getFluid();

    /**
     * @return the amount of fluid.
     *
     * @since 10.1.0
     */
    long getAmount();

    /**
     * @return optionally any {@link CompoundTag} extra data for this fluid ingredient.
     *
     * @since 10.1.0
     */
    Optional<CompoundTag> getTag();
}
