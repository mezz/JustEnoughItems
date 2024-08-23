package mezz.jei.library.plugins.debug;

import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.common.util.RegistryUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

public class FluidSubtypeHandlerTest<T> implements ISubtypeInterpreter<T> {
	private final IIngredientTypeWithSubtypes<Fluid, T> fluidType;

	public FluidSubtypeHandlerTest(IIngredientTypeWithSubtypes<Fluid, T> fluidType) {
		this.fluidType = fluidType;
	}

	@Override
	public @Nullable Object getSubtypeData(T ingredient, UidContext context) {
		return fluidType.getBase(ingredient);
	}

	@Override
	public String getLegacyStringSubtypeInfo(T fluidStack, UidContext context) {
		Fluid fluid = fluidType.getBase(fluidStack);
		ResourceLocation key = RegistryUtil
			.getRegistry(Registries.FLUID)
			.getKey(fluid);
		if (key == null) {
			throw new IllegalArgumentException("Fluid has no registry key: " + fluid);
		}
		return key.toString();
	}
}
