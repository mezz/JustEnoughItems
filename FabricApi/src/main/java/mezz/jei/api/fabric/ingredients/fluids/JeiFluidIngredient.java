package mezz.jei.api.fabric.ingredients.fluids;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * A simple record type that implements {@link IJeiFluidIngredient}
 *
 * @since 15.8.6
 */
public record JeiFluidIngredient(Fluid fluid, long amount, @Nullable CompoundTag tag) implements IJeiFluidIngredient {
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
