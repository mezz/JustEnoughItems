package mezz.jei.plugins.vanilla.ingredients.fluid;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.item.Item;

public final class FluidStackListFactory {
	private FluidStackListFactory() {

	}

	public static List<FluidStack> create() {
		List<FluidStack> fluidStacks = new ArrayList<>();

		Map<String, Fluid> registeredFluids = FluidRegistry.getRegisteredFluids();
		for (Fluid fluid : registeredFluids.values()) {
			Block fluidBlock = fluid.getBlock();
			if (Item.getItemFromBlock(fluidBlock) == Items.AIR) {
				FluidStack fluidStack = new FluidStack(fluid, Fluid.BUCKET_VOLUME);
				fluidStacks.add(fluidStack);
			}
		}

		return fluidStacks;
	}
}
