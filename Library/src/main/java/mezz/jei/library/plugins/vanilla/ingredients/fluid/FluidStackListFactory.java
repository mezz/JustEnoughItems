package mezz.jei.library.plugins.vanilla.ingredients.fluid;

import mezz.jei.api.helpers.IPlatformFluidHelper;
import net.minecraft.core.Registry;
import net.minecraft.world.level.material.Fluid;

import java.util.List;

public final class FluidStackListFactory {
	private FluidStackListFactory() {

	}

	public static <T> List<T> create(Registry<Fluid> registry, IPlatformFluidHelper<T> helper) {
		return registry.listElements()
			.filter(holder -> {
				Fluid fluid = holder.value();
				return fluid.isSource(fluid.defaultFluidState());
			})
			.map(fluid -> helper.create(fluid, helper.bucketVolume()))
			.toList();
	}
}
