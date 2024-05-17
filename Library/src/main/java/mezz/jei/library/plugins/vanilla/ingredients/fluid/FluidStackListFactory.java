package mezz.jei.library.plugins.vanilla.ingredients.fluid;

import mezz.jei.api.helpers.IPlatformFluidHelper;
import mezz.jei.common.util.RegistryWrapper;
import net.minecraft.world.level.material.Fluid;

import java.util.List;

public final class FluidStackListFactory {
	private FluidStackListFactory() {

	}

	public static <T> List<T> create(RegistryWrapper<Fluid> registry, IPlatformFluidHelper<T> helper) {
		return registry.getHolderStream()
			.filter(holder -> {
				Fluid fluid = holder.value();
				return fluid.isSource(fluid.defaultFluidState());
			})
			.map(holder -> helper.create(holder, helper.bucketVolume()))
			.toList();
	}
}
