package mezz.jei.api.registration;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Tell JEI how to interpret NBT tags and capabilities when comparing and looking up ingredients.
 *
 * If your ingredient has subtypes that depend on NBT or capabilities,
 * use this so JEI can tell those subtypes apart.
 */
public interface ISubtypeRegistration {

	/**
	 * Add an interpreter to allow JEI to understand the differences between ingredient subtypes.
	 * This interpreter should account for nbt and anything else
	 * that's relevant to differentiating the ingredient's subtypes.
	 *
	 * @param type        the ingredient type (for example {@link VanillaTypes#ITEM_STACK}
	 * @param base        the base of the ingredient that has subtypes (for example, {@link Items#ENCHANTED_BOOK}).
	 *                       All ingredients with this base will use the given interpreter.
	 * @param interpreter the interpreter for the ingredient's subtypes
	 *
	 * @since 9.7.0
	 */
	<B, I> void registerSubtypeInterpreter(IIngredientTypeWithSubtypes<B, I> type, B base, IIngredientSubtypeInterpreter<I> interpreter);

	/**
	 * Tells JEI to treat all NBT as relevant to these items' subtypes.
	 */
	void useNbtForSubtypes(Item... items);

	/**
	 * Tells JEI to treat all NBT as relevant to these fluids' subtypes.
	 */
	void useNbtForSubtypes(Fluid... fluids);

	/**
	 * Add an interpreter to compare item subtypes.
	 * This interpreter should account for nbt and anything else that's relevant to differentiating the item's subtypes.
	 *
	 * @param item        the item that has subtypes.
	 * @param interpreter the interpreter for the item.
	 * @since 7.6.2
	 *
	 * @deprecated use {@link #registerSubtypeInterpreter(IIngredientTypeWithSubtypes, Object, IIngredientSubtypeInterpreter)}
	 */
	@Deprecated(forRemoval = true, since = "9.7.0")
	default void registerSubtypeInterpreter(Item item, IIngredientSubtypeInterpreter<ItemStack> interpreter) {
		registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, item, interpreter);
	}

	/**
	 * Returns whether an {@link IIngredientSubtypeInterpreter} has been registered for this item.
	 *
	 * @deprecated no longer used
	 */
	@Deprecated(forRemoval = true, since = "9.7.0")
	boolean hasSubtypeInterpreter(ItemStack itemStack);
}
