package mezz.jei.api.gui;

import javax.annotation.Nonnull;
import java.util.Collection;

import net.minecraftforge.fluids.FluidStack;

/**
 * IGuiFluidStackGroup displays FluidStacks in a gui.
 *
 * If multiple FluidStacks are set, they will be displayed in rotation.
 */
public interface IGuiFluidStackGroup extends IGuiIngredientGroup<FluidStack> {

	/**
	 * Fluid tanks must be initialized once, and then can be set many times.
	 */
	void init(int index, int xPosition, int yPosition, int width, int height, int capacityMb);

	@Override
	void set(int index, @Nonnull Collection<FluidStack> fluidStacks);

	@Override
	void set(int index, @Nonnull FluidStack fluidStack);

}
