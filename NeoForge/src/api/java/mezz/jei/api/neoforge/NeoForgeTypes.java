package mezz.jei.api.neoforge;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;

/**
 * Built-in {@link IIngredientType} for NeoForge Minecraft.
 *
 * @since 17.0.0
 */
public final class NeoForgeTypes {
	/**
	 * @since 17.0.0
	 */
	public static final IIngredientTypeWithSubtypes<Fluid, FluidStack> FLUID_STACK = new IIngredientTypeWithSubtypes<>() {
		@Override
		public String getUid() {
			return "fluid_stack";
		}

		@Override
		public Class<? extends FluidStack> getIngredientClass() {
			return FluidStack.class;
		}

		@Override
		public Class<? extends Fluid> getIngredientBaseClass() {
			return Fluid.class;
		}

		@Override
		public Fluid getBase(FluidStack ingredient) {
			return ingredient.getFluid();
		}

		@Override
		public FluidStack getDefaultIngredient(Fluid base) {
			return new FluidStack(base, FluidType.BUCKET_VOLUME);
		}
	};

	private NeoForgeTypes() {}
}
