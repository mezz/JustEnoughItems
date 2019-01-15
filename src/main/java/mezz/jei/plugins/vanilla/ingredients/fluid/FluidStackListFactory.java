package mezz.jei.plugins.vanilla.ingredients.fluid;

import java.util.ArrayList;
import java.util.List;

import net.minecraftforge.fluids.FluidStack;

public final class FluidStackListFactory {
	private FluidStackListFactory() {

	}

	public static List<FluidStack> create() {
		List<FluidStack> fluidStacks = new ArrayList<>();

//		Map<String, Fluid> registeredFluids = FluidRegistry.getRegisteredFluids();
//		for (Fluid fluid : registeredFluids.values()) {
//			Block fluidBlock = fluid.getBlock();
//			if (Item.getItemFromBlock(fluidBlock) == Items.AIR) {
//				FluidStack fluidStack = new FluidStack(fluid, Fluid.BUCKET_VOLUME);
//				fluidStacks.add(fluidStack);
//			}
//		}

		return fluidStacks;
	}
}
