package mezz.jei.api.ingredients;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IModIngredientRegistration;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Optional;

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

	/**
	 * @return The unique ID for this type, used for serialization to and from disk.
	 * @since 15.5.0
	 */
	default String getUid() {
		Class<? extends T> ingredientClass = getIngredientClass();
		return ingredientClass.getName();
	}

	/**
	 * Helper to cast an unknown ingredient to this type if it matches.
	 *
	 * @since 11.5.0
	 */
	default Optional<T> castIngredient(@Nullable Object ingredient) {
		Class<? extends T> ingredientClass = getIngredientClass();
		return Optional.ofNullable(ingredient)
			.filter(ingredientClass::isInstance)
			.map(ingredientClass::cast);
	}
}
