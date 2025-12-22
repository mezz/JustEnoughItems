package mezz.jei.library.plugins.debug;

import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.world.level.material.Fluid;
import org.jspecify.annotations.Nullable;

public class FluidSubtypeHandlerTest<T> implements ISubtypeInterpreter<T> {
	private final IIngredientTypeWithSubtypes<Fluid, T> fluidType;

	public FluidSubtypeHandlerTest(IIngredientTypeWithSubtypes<Fluid, T> fluidType) {
		this.fluidType = fluidType;
	}

	@Override
	public @Nullable Object getSubtypeData(T ingredient, UidContext context) {
		return fluidType.getBase(ingredient);
	}
}
