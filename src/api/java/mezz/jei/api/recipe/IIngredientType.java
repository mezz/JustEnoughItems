package mezz.jei.api.recipe;

import java.util.Collection;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IModIngredientRegistration;
import mezz.jei.api.ingredients.VanillaTypes;

/**
 * A type of ingredient (i.e. ItemStack, FluidStack, etc) handled by JEI.
 * Register new types with {@link IModIngredientRegistration#register(IIngredientType, Collection, IIngredientHelper, IIngredientRenderer)}
 *
 * @see VanillaTypes for the built-in vanilla types {@link VanillaTypes#ITEM} and {@link VanillaTypes#FLUID}
 * @since JEI 4.12.0
 */
public interface IIngredientType<T> {
	/**
	 * @return The class of the ingredient for this type.
	 */
	Class<? extends T> getIngredientClass();
}
