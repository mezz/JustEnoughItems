package mezz.jei.api.fabric.constants;

import mezz.jei.api.fabric.ingredients.fluids.IJeiFluidIngredient;
import mezz.jei.api.fabric.ingredients.fluids.JeiFluidIngredient;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.world.level.material.Fluid;

/**
 * Built-in {@link IIngredientType} for Fabric Minecraft.
 *
 * @since 10.1.0
 */
public final class FabricTypes {
	/**
	 * @since 10.1.0
	 */
	public static final IIngredientTypeWithSubtypes<Fluid, IJeiFluidIngredient> FLUID_STACK = new IIngredientTypeWithSubtypes<>() {
		@Override
		public String getUid() {
			return "fluid_stack";
		}

		@Override
		public Class<? extends IJeiFluidIngredient> getIngredientClass() {
			return IJeiFluidIngredient.class;
		}

		@Override
		public Class<? extends Fluid> getIngredientBaseClass() {
			return Fluid.class;
		}

		@Override
		public Fluid getBase(IJeiFluidIngredient ingredient) {
			return ingredient.getFluid();
		}

		@SuppressWarnings("UnstableApiUsage")
		@Override
		public IJeiFluidIngredient getDefaultIngredient(Fluid base) {
			return new JeiFluidIngredient(base, FluidConstants.BUCKET, null);
		}
	};

	private FabricTypes() {}
}
