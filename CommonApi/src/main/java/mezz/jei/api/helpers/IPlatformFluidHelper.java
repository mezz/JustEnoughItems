package mezz.jei.api.helpers;

import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.level.material.Fluid;

/**
 * Helper for mods that want to handle Fluid ingredients across multiple platforms (Forge and Fabric).
 * @param <T> the type of Fluid ingredient for the current platform.
 * @since 10.1.0
 */
public interface IPlatformFluidHelper<T> {
	/**
	 * Returns the type of Fluid ingredients on the current platform.
	 * @since 10.1.0
	 */
	IIngredientTypeWithSubtypes<Fluid, T> getFluidIngredientType();

	/**
	 * Creates a new fluid ingredient for the current platform.
	 * @since 18.0.0
	 */
	T create(Holder<Fluid> fluid, long amount, DataComponentPatch components);

	/**
	 * Creates a new fluid ingredient for the current platform.
	 * @since 18.0.0
	 */
	T create(Holder<Fluid> fluid, long amount);

	/**
	 * Returns amount of Fluid in one bucket on the current platform.
	 * @since 10.1.0
	 */
	long bucketVolume();

	String unit();
}
