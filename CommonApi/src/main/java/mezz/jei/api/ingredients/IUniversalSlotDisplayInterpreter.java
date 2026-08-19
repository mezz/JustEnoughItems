package mezz.jei.api.ingredients;

import mezz.jei.api.registration.ISlotDisplayInterpreterRegistration;
import net.minecraft.world.item.crafting.display.SlotDisplay;

/**
 * Interprets a {@link SlotDisplay} independently of any ingredient type.
 * <p>
 * Universal interpreters are useful for displays such as composites and transparent wrappers that delegate to other
 * slot displays in the same way for every ingredient type. JEI applies the universal interpreter first, then applies
 * any interpreter registered for the current ingredient type.
 * <p>
 * Register universal interpreters with
 * {@link ISlotDisplayInterpreterRegistration#registerUniversal}.
 *
 * @param <D> the type of slot display
 *
 * @since 30.20.0
 */
@FunctionalInterface
public interface IUniversalSlotDisplayInterpreter<D extends SlotDisplay> {
	/**
	 * Interpret this display without depending on a resolved ingredient type.
	 *
	 * @param slotDisplay the slot display being interpreted
	 * @param interpretationBuilder describes wrapped or child displays and any shared interpretation information
	 *
	 * @since 30.20.0
	 */
	void interpret(D slotDisplay, ISlotDisplayInterpretationBuilder interpretationBuilder);
}
