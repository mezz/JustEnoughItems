package mezz.jei.library.plugins.debug;

import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.common.util.RegistryUtil;
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
		ResourceLocation key = RegistryUtil
			.getRegistry(Registries.FLUID)
			.getKey(fluid);
		if (key == null) {
			throw new IllegalArgumentException("Fluid has no registry key: " + fluid);
		}
		return key.toString();
	}
}
