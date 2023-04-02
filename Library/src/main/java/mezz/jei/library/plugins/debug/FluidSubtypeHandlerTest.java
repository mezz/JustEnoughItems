package mezz.jei.library.plugins.debug;

import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.common.platform.Services;
import net.minecraft.core.registries.Registries;
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
		return Services.PLATFORM
			.getRegistry(Registries.FLUID)
			.getRegistryName(fluid)
			.map(ResourceLocation::toString)
			.orElseThrow(() -> new IllegalArgumentException("Fluid has no registry name: " + fluid));
	}
}
