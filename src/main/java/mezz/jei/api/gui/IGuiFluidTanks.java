package mezz.jei.api.gui;

import javax.annotation.Nonnull;

import net.minecraftforge.fluids.FluidStack;

/**
 * IGuiFluidTanks displays FluidStack Tanks in a gui.
 *
 * If multiple FluidStacks are set, they will be displayed in rotation.
 */
public interface IGuiFluidTanks {

	/**
	 * Fluid tanks must be initialized once, and then can be set many times.
	 */
	void init(int index, int xPosition, int yPosition, int capacityMb);

	void set(int index, @Nonnull Iterable<FluidStack> fluidStacks);

	void set(int index, @Nonnull FluidStack fluidStacks);

}
