package mezz.jei.api.ingredients.subtypes;

import javax.annotation.Nullable;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import mezz.jei.api.registration.ISubtypeRegistration;

/**
 * Gets subtype information from ingredients that have subtype interpreters.
 *
 * Add subtypes for your ingredients with {@link ISubtypeRegistration#registerSubtypeInterpreter(Item, ISubtypeInterpreter)}.
 */
public interface ISubtypeManager {
	/**
	 * Get the data from an itemStack that is relevant to comparing and telling subtypes apart.
	 * Returns null if the itemStack has no information used for subtypes.
	 *
	 * @deprecated since JEI 7.3.0. Use {@link #getSubtypeInfo(ItemStack, UidContext)}
	 */
	@Nullable
	@Deprecated
	String getSubtypeInfo(ItemStack itemStack);

	/**
	 * Get the data from an itemStack that is relevant to comparing and telling subtypes apart.
	 * Returns null if the itemStack has no information used for subtypes.
	 *
	 * @since JEI 7.3.0
	 */
	@Nullable
	String getSubtypeInfo(ItemStack itemStack, UidContext context);
}
