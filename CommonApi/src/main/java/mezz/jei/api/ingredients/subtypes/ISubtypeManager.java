package mezz.jei.api.ingredients.subtypes;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * Gets subtype information from ingredients that have subtype interpreters.
 * <p>
 * Add subtypes for your ingredients with {@link ISubtypeRegistration#registerSubtypeInterpreter(IIngredientTypeWithSubtypes, Object, ISubtypeInterpreter)}.
 */
public interface ISubtypeManager {
	/**
	 * Get the data from an ItemStack that is relevant to comparing and telling subtypes apart.
	 * Returns null if the ItemStack has no information used for subtypes.
	 *
	 * @since 19.9.0
	 */
	@Nullable
	default Object getSubtypeData(ItemStack ingredient, UidContext context) {
		return getSubtypeData(VanillaTypes.ITEM_STACK, ingredient, context);
	}

	/**
	 * Get the data from an ingredient that is relevant to comparing and telling subtypes apart.
	 * Returns null if the ingredient has no information used for subtypes.
	 *
	 * @since 19.9.0
	 */
	@Nullable
	<T> Object getSubtypeData(IIngredientTypeWithSubtypes<?, T> ingredientType, T ingredient, UidContext context);

	/**
	 * Get the data from a typed ingredient that is relevant to comparing and telling subtypes apart.
	 * Returns null if the typed ingredient has no information used for subtypes.
	 *
	 * @since 19.19.4
	 */
	@Nullable
	<B, T> Object getSubtypeData(IIngredientTypeWithSubtypes<B, T> ingredientType, ITypedIngredient<T> typedIngredient, UidContext context);

	/**
	 * Return true if the given ItemStack can have subtypes.
	 * For example in the vanilla game an enchanted book may have subtypes, but an apple does not.
	 *
	 * @see ISubtypeRegistration#registerSubtypeInterpreter
	 * @see ISubtypeManager#getSubtypeData
	 *
	 * @since 21.2.0
	 */
	default boolean hasSubtypes(ItemStack ingredient) {
		return hasSubtypes(VanillaTypes.ITEM_STACK, ingredient);
	}

	/**
	 * Return true if the given ingredient can have subtypes.
	 * For example in the vanilla game an enchanted book may have subtypes, but an apple does not.
	 *
	 * @see ISubtypeRegistration#registerSubtypeInterpreter
	 * @see ISubtypeManager#getSubtypeData
	 *
	 * @since 19.3.0
	 */
	<T, B> boolean hasSubtypes(IIngredientTypeWithSubtypes<B, T> ingredientType, T ingredient);
}
