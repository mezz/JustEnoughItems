package mezz.jei.common.plugins.debug;

import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.common.platform.IPlatformRegistry;
import mezz.jei.common.platform.Services;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;

public class FluidSubtypeHandlerTest<T> implements IIngredientSubtypeInterpreter<T> {
	private final IIngredientTypeWithSubtypes<Fluid, T> type;

	public FluidSubtypeHandlerTest(IIngredientTypeWithSubtypes<Fluid, T> type) {
		this.type = type;
	}

	@Override
	public String apply(T fluidStack, UidContext context) {
		Fluid fluid = type.getBase(fluidStack);
		IPlatformRegistry<Fluid> registry = Services.PLATFORM.getRegistry(Registry.FLUID_REGISTRY);
		ResourceLocation name = registry.getRegistryName(fluid);
		if (name != null) {
			return name.toString();
		} else {
			throw new IllegalArgumentException("Fluid has no registry name! " + fluid);
		}
	}
}
