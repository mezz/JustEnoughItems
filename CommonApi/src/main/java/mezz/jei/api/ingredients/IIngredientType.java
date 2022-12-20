package mezz.jei.api.ingredients;

import java.util.Collection;
import java.util.Optional;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IModIngredientRegistration;
import org.jetbrains.annotations.Nullable;

/**
 * A type of ingredient (i.e. ItemStack, FluidStack, etc) handled by JEI.
 * Register new types with {@link IModIngredientRegistration#register(IIngredientType, Collection, IIngredientHelper, IIngredientRenderer)}
 *
 * @see VanillaTypes for the built-in vanilla type {@link VanillaTypes#ITEM_STACK}
 */
@FunctionalInterface
public interface IIngredientType<T> {
	/**
	 * @return The class of the ingredient for this type.
	 */
	Class<? extends T> getIngredientClass();

	default Optional<T> castIngredient(@Nullable Object ingredient) {
		Class<? extends T> ingredientClass = getIngredientClass();
		return Optional.ofNullable(ingredient)
			.filter(ingredientClass::isInstance)
			.map(ingredientClass::cast);
	}
}
