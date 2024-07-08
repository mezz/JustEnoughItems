package mezz.jei.api.ingredients.subtypes;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.world.item.ItemStack;

/**
 * Gets subtype information from ingredients that have subtype interpreters.
 * <p>
 * Add subtypes for your ingredients with {@link ISubtypeRegistration#registerSubtypeInterpreter(IIngredientTypeWithSubtypes, Object, IIngredientSubtypeInterpreter)}.
 */
public interface ISubtypeManager {
	/**
	 * Get the data from an ItemStack that is relevant to comparing and telling subtypes apart.
	 * Returns {@link IIngredientSubtypeInterpreter#NONE} if the ItemStack has no information used for subtypes.
	 *
	 * @since 11.1.1
	 */
	default String getSubtypeInfo(ItemStack ingredient, UidContext context) {
		return getSubtypeInfo(VanillaTypes.ITEM_STACK, ingredient, context);
	}

	/**
	 * Get the data from an ingredient that is relevant to comparing and telling subtypes apart.
	 * Returns {@link IIngredientSubtypeInterpreter#NONE} if the ingredient has no information used for subtypes.
	 *
	 * @since 9.7.0
	 */
	<T> String getSubtypeInfo(IIngredientTypeWithSubtypes<?, T> ingredientType, T ingredient, UidContext context);

	/**
	 * Return true if the given ingredient can have subtypes.
	 * For example in the vanilla game an enchanted book may have subtypes, but an apple does not.
	 *
	 * @see ISubtypeRegistration#registerSubtypeInterpreter
	 * @see ISubtypeManager#getSubtypeInfo
	 *
	 * @since 19.3.0
	 */
	<T, B> boolean hasSubtypes(IIngredientTypeWithSubtypes<B, T> ingredientType, T ingredient);
}
