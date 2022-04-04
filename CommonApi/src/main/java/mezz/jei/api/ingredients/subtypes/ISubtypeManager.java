package mezz.jei.api.ingredients.subtypes;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Gets subtype information from ingredients that have subtype interpreters.
 *
 * Add subtypes for your ingredients with {@link ISubtypeRegistration#registerSubtypeInterpreter(IIngredientTypeWithSubtypes, Object, IIngredientSubtypeInterpreter)}.
 */
public interface ISubtypeManager {
	/**
	 * Get the data from an ingredient that is relevant to comparing and telling subtypes apart.
	 * Returns {@link IIngredientSubtypeInterpreter#NONE} if the ingredient has no information used for subtypes.
	 *
	 * @since 9.7.0
	 */
	<T> String getSubtypeInfo(IIngredientTypeWithSubtypes<?, T> ingredientType, T ingredient, UidContext context);

	/**
	 * Get the data from an ingredient that is relevant to comparing and telling subtypes apart.
	 * Returns null if the ingredient has no information used for subtypes.
	 *
	 * @since 9.6.0
	 * @deprecated use {@link #getSubtypeInfo(IIngredientTypeWithSubtypes, Object, UidContext)}
	 */
	@Deprecated(forRemoval = true, since = "9.7.0")
	@Nullable
	default <T> String getSubtypeInfo(IIngredientType<T> ingredientType, T ingredient, UidContext context) {
		if (ingredientType instanceof IIngredientTypeWithSubtypes<?, T> ingredientTypeWithSubtypes) {
			String subtypeInfo = getSubtypeInfo(ingredientTypeWithSubtypes, ingredient, context);
			if (!subtypeInfo.isEmpty()) {
				return subtypeInfo;
			}
		}
		return null;
	}

	/**
	 * Get the data from an itemStack that is relevant to comparing and telling subtypes apart.
	 * Returns null if the itemStack has no information used for subtypes.
	 *
	 * @since 7.3.0
	 * @deprecated use {@link #getSubtypeInfo(IIngredientType, Object, UidContext)}
	 */
	@Deprecated(forRemoval = true, since = "9.6.0")
	@Nullable
	default String getSubtypeInfo(ItemStack itemStack, UidContext context) {
		String subtypeInfo = getSubtypeInfo(VanillaTypes.ITEM_STACK, itemStack, context);
		if (subtypeInfo.isEmpty()) {
			return null;
		}
		return subtypeInfo;
	}
}
