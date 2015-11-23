package mezz.jei.api.recipe;

import javax.annotation.Nonnull;
import java.util.List;

import net.minecraft.client.Minecraft;

import net.minecraftforge.fluids.FluidStack;

public interface IRecipeWrapper {

	List getInputs();

	List<FluidStack> getFluidInputs();

	List getOutputs();

	List<FluidStack> getFluidOutputs();

	/* Draw additional info about the recipe. */
	void drawInfo(@Nonnull Minecraft minecraft);
}
