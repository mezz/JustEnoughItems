package mezz.jei.fabric.ingredients.fluid;

import mezz.jei.api.fabric.ingredients.fluids.IJeiFluidIngredient;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluid;

import javax.annotation.Nullable;
import java.util.Optional;

public final class JeiFluidIngredient implements IJeiFluidIngredient {
    private final Fluid fluid;
    private final long amount;
    @Nullable
    private final CompoundTag tag;

    public JeiFluidIngredient(Fluid fluid, long amount) {
        this.fluid = fluid;
        this.amount = amount;
        this.tag = null;
    }

    public JeiFluidIngredient(Fluid fluid, long amount, @Nullable CompoundTag tag) {
        this.fluid = fluid;
        this.amount = amount;
        if (tag != null) {
            this.tag = tag.copy();
        } else {
            this.tag = null;
        }
    }

    @Override
    public Fluid getFluid() {
        return fluid;
    }

    @Override
    public long getAmount() {
        return amount;
    }

    @Override
    public Optional<CompoundTag> getTag() {
        return Optional.ofNullable(tag);
    }
}
