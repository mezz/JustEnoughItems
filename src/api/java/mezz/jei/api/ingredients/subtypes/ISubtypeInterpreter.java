package mezz.jei.api.ingredients.subtypes;

import java.util.function.Function;

import net.minecraft.item.ItemStack;

@FunctionalInterface
public interface ISubtypeInterpreter extends Function<ItemStack, String> {
	String NONE = "";

	/**
	 * Get the data from an itemStack that is relevant to telling subtypes apart.
	 * This should account for nbt, and anything else that's relevant.
	 * Return {@link #NONE} if there is no data used for subtypes.
	 */
	@Override
	String apply(ItemStack itemStack);
}
