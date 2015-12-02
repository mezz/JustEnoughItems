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
	 * @param slotIndex the slot index of this fluid
	 * @param input whether this slot is an input
	 * @param xPosition x position relative to the recipe background
	 * @param yPosition y position relative to the recipe background
	 * @param width width of this tank
	 * @param height height of this tank
	 * @param capacityMb maximum amount of fluid that this tank can hold
	 */
	void init(int slotIndex, boolean input, int xPosition, int yPosition, int width, int height, int capacityMb);

	@Override
	void set(int slotIndex, @Nonnull Collection<FluidStack> fluidStacks);

	@Override
	void set(int slotIndex, @Nonnull FluidStack fluidStack);

}
