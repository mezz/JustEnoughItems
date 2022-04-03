package mezz.jei.forge.plugins.forge.ingredients.fluid;

import java.util.Collection;
import java.util.List;

import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraft.world.level.material.Fluid;

public final class FluidStackListFactory {
	private FluidStackListFactory() {

	}

	public static List<FluidStack> create() {
		IForgeRegistry<Fluid> fluidRegistry = ForgeRegistries.FLUIDS;
		Collection<Fluid> fluids = fluidRegistry.getValues();
		return fluids.stream()
			.filter(fluid -> fluid.isSource(fluid.defaultFluidState()))
			.map(fluid -> new FluidStack(fluid, FluidAttributes.BUCKET_VOLUME))
			.toList();
	}
}
