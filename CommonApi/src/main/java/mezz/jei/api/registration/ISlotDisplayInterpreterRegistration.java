package mezz.jei.api.registration;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ISlotDisplayInterpreter;
import mezz.jei.api.ingredients.IUniversalSlotDisplayInterpreter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import org.jetbrains.annotations.ApiStatus;

/**
 * Register slot display interpreters so JEI can correctly match and describe resolved ingredients.
 * <p>
 * Use this for slot displays that lose important information when Minecraft resolves it into ordinary
 * ingredients. An interpreter can tell JEI that one resolved value stands for all subtypes, preserve a tag name
 * for tooltips, or add a heading that explains the display's broader meaning.
 * <p>
 * This is given to plugins in {@link IModPlugin#registerSlotDisplayInterpreters}.
 *
 * @since 27.26.0
 */
@ApiStatus.NonExtendable
public interface ISlotDisplayInterpreterRegistration {
	/**
	 * Register an interpreter that applies to a slot display for every ingredient type.
	 * <p>
	 * Use this for displays such as composites that always delegate to the same child displays, regardless of the
	 * ingredient type being resolved. JEI applies this interpreter before one registered for a specific ingredient
	 * type.
	 * <p>
	 * Use an ingredient-specific {@link #register} overload instead when the interpretation depends on a resolved
	 * ingredient type.
	 *
	 * @param slotDisplayType the type of slot display to interpret
	 * @param interpreter describes the display independently of a resolved ingredient type
	 *
	 * @since 27.26.0
	 */
	<D extends SlotDisplay> void registerUniversal(
		SlotDisplay.Type<D> slotDisplayType,
		IUniversalSlotDisplayInterpreter<D> interpreter
	);

	/**
	 * Register an interpreter for a slot display that resolves into item stacks.
	 * The interpreter can get all item stacks produced by the display from its context.
	 *
	 * @param slotDisplayType the type of slot display to interpret
	 * @param interpreter describes how JEI should match and present the resolved item stacks
	 *
	 * @since 27.26.0
	 */
	default <D extends SlotDisplay> void register(
		SlotDisplay.Type<D> slotDisplayType,
		ISlotDisplayInterpreter<D, ItemStack> interpreter
	) {
		register(slotDisplayType, VanillaTypes.ITEM_STACK, interpreter);
	}

	/**
	 * Register an interpreter for one slot display type and ingredient type.
	 * The interpreter can get all ingredients of this type produced by the display from its context.
	 *
	 * @param slotDisplayType the type of slot display to interpret
	 * @param ingredientType the type of ingredient produced by the display
	 * @param interpreter describes how JEI should match and present the resolved ingredients
	 *
	 * @since 27.26.0
	 */
	<D extends SlotDisplay, T> void register(
		SlotDisplay.Type<D> slotDisplayType,
		IIngredientType<T> ingredientType,
		ISlotDisplayInterpreter<D, T> interpreter
	);
}
