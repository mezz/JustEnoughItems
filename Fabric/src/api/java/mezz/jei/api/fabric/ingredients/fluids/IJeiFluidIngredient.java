package mezz.jei.api.fabric.ingredients.fluids;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import org.jetbrains.annotations.ApiStatus;

/**
 * Built-in ingredient for representing Fluids in Fabric Minecraft.
 *
 * @since 10.1.0
 */
@ApiStatus.NonExtendable
public interface IJeiFluidIngredient {
	/**
	 * @return the fluid variant represented by this ingredient.
	 *
	 * @since 18.0.0
	 */
	FluidVariant getFluidVariant();

	/**
	 * @return the amount of fluid.
	 *
	 * @since 10.1.0
	 */
	long getAmount();
}
