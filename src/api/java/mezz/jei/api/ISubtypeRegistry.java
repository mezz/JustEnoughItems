package mezz.jei.api;

import javax.annotation.Nullable;
import java.util.function.Function;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * Tell JEI how to interpret NBT tags and capabilities when comparing and looking up items.
 * <p>
 * Some items have subtypes, most of them use meta values for this and JEI handles them by default.
 * If your item has subtypes that depend on NBT or capabilities instead of meta, use this interface so JEI can tell those subtypes apart.
 * <p>
 * Note: JEI has built-in support for differentiating items that implement {@link net.minecraftforge.fluids.capability.CapabilityFluidHandler},
 * adding a subtype interpreter here will override that functionality.
 * <p>
 * Get the instance by implementing {@link IModPlugin#registerItemSubtypes(ISubtypeRegistry)}.
 *
 * @since 3.6.4
 */
public interface ISubtypeRegistry {
	/**
	 * Tells JEI to treat all NBT as relevant to these items' subtypes.
	 */
	void useNbtForSubtypes(Item... items);

	/**
	 * Add an interpreter to compare item subtypes.
	 *
	 * @param item        the item that has subtypes.
	 * @param interpreter the interpreter for the item.
	 * @deprecated since JEI 3.13.5. This is being renamed to {@link #registerSubtypeInterpreter(Item, ISubtypeInterpreter)}
	 */
	@Deprecated
	void registerNbtInterpreter(Item item, ISubtypeInterpreter interpreter);

	/**
	 * Add an interpreter to compare item subtypes.
	 * This interpreter should account for meta, nbt, and anything else that's relevant to differentiating the item's subtypes.
	 *
	 * @param item        the item that has subtypes.
	 * @param interpreter the interpreter for the item.
	 * @since JEI 3.13.5
	 */
	void registerSubtypeInterpreter(Item item, ISubtypeInterpreter interpreter);

	/**
	 * Get the data from an itemStack that is relevant to comparing and telling subtypes apart.
	 * Returns null if the itemStack has no information used for subtypes.
	 */
	@Nullable
	String getSubtypeInfo(ItemStack itemStack);

	/**
	 * Returns whether an {@link ISubtypeInterpreter} has been registered for this item.
	 *
	 * @since JEI 4.1.1
	 */
	boolean hasSubtypeInterpreter(ItemStack itemStack);

	@FunctionalInterface
	interface ISubtypeInterpreter extends Function<ItemStack, String> {
		String NONE = "";

		/**
		 * Get the data from an itemStack that is relevant to telling subtypes apart.
		 * This should account for meta, nbt, and anything else that's relevant.
		 * Return {@link #NONE} if there is no data used for subtypes.
		 *
		 * @since JEI 4.7.8
		 */
		@Override
		String apply(ItemStack itemStack);

		/**
		 * Get the data from an itemStack that is relevant to telling subtypes apart.
		 * This should account for meta, nbt, and anything else that's relevant.
		 * Returns null if there is no data used for subtypes.
		 *
		 * @deprecated since JEI 4.7.8
		 */
		@Deprecated
		@Nullable
		default String getSubtypeInfo(ItemStack itemStack) {
			return apply(itemStack);
		}
	}
}
