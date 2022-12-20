package mezz.jei.api.ingredients;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

/**
 * Ingredient with type information, for type safety.
 * These ingredients are validated by JEI, and only contain valid types and ingredients.
 *
 * Create an instance with {@link IIngredientManager#createTypedIngredient}.
 *
 * @since 9.3.0
 */
public interface ITypedIngredient<T> {
	/**
	 * @return the type of this ingredient
	 * @see IIngredientType
	 *
	 * @since 9.3.0
	 */
	IIngredientType<T> getType();

	/**
	 * @return the ingredient wrapped by this instance
	 *
	 * @since 9.3.0
	 */
	T getIngredient();

	/**
	 * @return the ingredient wrapped by this instance, only if it matches the given type.
	 * This is useful when handling a wildcard generic instance of `ITypedIngredient<?>`.
	 *
	 * @since 9.3.3
	 */
	default <V> Optional<V> getIngredient(IIngredientType<V> ingredientType) {
		return ingredientType.castIngredient(getIngredient());
	}

	/**
	 * @return the ItemStack wrapped by this instance, only this holds an ItemStack ingredient.
	 * This is useful when handling a wildcard generic instance of `ITypedIngredient<?>`.
	 *
	 * @see #getIngredient(IIngredientType) to get other ingredient types
	 * @since 11.1.1
	 */
	default Optional<ItemStack> getItemStack() {
		return getIngredient(VanillaTypes.ITEM_STACK);
	}
}
