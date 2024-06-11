package mezz.jei.fabric.ingredients.fluid;

import mezz.jei.api.fabric.ingredients.fluids.IJeiFluidIngredient;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;

public final class JeiFluidIngredient implements IJeiFluidIngredient {
	private final FluidVariant fluid;
	private final long amount;

	public JeiFluidIngredient(FluidVariant fluid, long amount) {
		this.fluid = fluid;
		this.amount = amount;
	}

	@Override
	public FluidVariant getFluidVariant() {
		return fluid;
	}

	@Override
	public long getAmount() {
		return amount;
	}
}
