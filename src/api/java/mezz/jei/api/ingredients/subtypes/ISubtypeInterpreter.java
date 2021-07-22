package mezz.jei.api.ingredients.subtypes;

import net.minecraft.item.ItemStack;

import java.util.function.Function;

/**
 * @deprecated since JEI 7.6.2, use {@link IIngredientSubtypeInterpreter} instead
 */
@Deprecated
@FunctionalInterface
public interface ISubtypeInterpreter extends IIngredientSubtypeInterpreter<ItemStack>, Function<ItemStack, String> {
	/** @deprecated use {@link IIngredientSubtypeInterpreter#NONE} */
	@Deprecated
	String NONE = IIngredientSubtypeInterpreter.NONE;

	/**
	 * Get the data from an itemStack that is relevant to telling subtypes apart.
	 * This should account for nbt, and anything else that's relevant.
	 * Return {@link #NONE} if there is no data used for subtypes.
	 */
	@Override
	String apply(ItemStack itemStack);

	/**
	 * Get the data from an itemStack that is relevant to telling subtypes apart in the given context.
	 * This should account for nbt, and anything else that's relevant.
	 * Return {@link #NONE} if there is no data used for subtypes.
	 * @since JEI 7.3.0
	 */
	@Override
	default String apply(ItemStack itemStack, UidContext context) {
		return apply(itemStack);
	}
}
