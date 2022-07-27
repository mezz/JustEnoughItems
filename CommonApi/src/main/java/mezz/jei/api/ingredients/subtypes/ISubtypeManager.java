package mezz.jei.api.ingredients.subtypes;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.world.item.ItemStack;

/**
 * Gets subtype information from ingredients that have subtype interpreters.
 *
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
}
