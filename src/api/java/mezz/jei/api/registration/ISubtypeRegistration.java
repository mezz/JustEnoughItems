package mezz.jei.api.registration;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;

/**
 * Tell JEI how to interpret NBT tags and capabilities when comparing and looking up items.
 *
 * If your item has subtypes that depend on NBT or capabilities, use this so JEI can tell those subtypes apart.
 */
public interface ISubtypeRegistration {
	/**
	 * Add an interpreter to compare item subtypes.
	 * This interpreter should account for nbt and anything else that's relevant to differentiating the item's subtypes.
	 *
	 * @param item        the item that has subtypes.
	 * @param interpreter the interpreter for the item.
	 */
	void registerSubtypeInterpreter(Item item, ISubtypeInterpreter interpreter);

	/**
	 * Tells JEI to treat all NBT as relevant to these items' subtypes.
	 */
	void useNbtForSubtypes(Item... items);

	/**
	 * Returns whether an {@link ISubtypeInterpreter} has been registered for this item.
	 */
	boolean hasSubtypeInterpreter(ItemStack itemStack);
}
