package mezz.jei.api.fabric.ingredients.fluids;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;

/**
 * A simple record type that implements {@link IJeiFluidIngredient}
 *
 * @since 19.5.6
 */
public record JeiFluidIngredient(FluidVariant fluid, long amount) implements IJeiFluidIngredient {
	@Override
	public FluidVariant getFluidVariant() {
		return fluid;
	}

	@Override
	public long getAmount() {
		return amount;
	}
}
