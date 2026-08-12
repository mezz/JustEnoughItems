package mezz.jei.api.ingredients;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.registration.ISlotDisplayInterpreterRegistration;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.stream.Stream;

/**
 * Tells JEI what a {@link SlotDisplay} means for the ingredients produced by that display.
 * <p>
 * Minecraft resolves slot displays into ordinary values such as item stacks. Sometimes the values do not tell
 * the whole story. An item display may produce one default potion stack even though the recipe accepts any potion,
 * and a tag display may produce stacks without preserving which tag they came from.
 * <p>
 * An interpreter supplies the missing information so JEI can find the recipe from ingredient lookups,
 * match focused ingredients to recipe slots, and show accurate tooltips. Interpreters do not create ingredient
 * values directly; JEI still uses Minecraft's normal {@link SlotDisplay#resolve} behavior for the display or
 * for the child displays declared by the interpreter.
 * <p>
 * Register interpreters with {@link ISlotDisplayInterpreterRegistration} from
 * {@link IModPlugin#registerSlotDisplayInterpreters}.
 *
 * @param <D> the type of slot display
 * @param <T> the type of ingredient
 *
 * @since 30.20.0
 */
@FunctionalInterface
public interface ISlotDisplayInterpreter<D extends SlotDisplay, T> {
	/**
	 * Describe how JEI should handle the ingredients produced by this slot display.
	 *
	 * @param slotDisplay the slot display being interpreted
	 * @param context lazily resolved ingredients, helpers for nested displays, and information about the recipe slot
	 * @param interpretationBuilder describes delegated displays and any meaning lost during resolution
	 *
	 * @since 30.20.0
	 */
	void interpret(
		D slotDisplay,
		IContext<T> context,
		ISlotDisplayInterpretationBuilder interpretationBuilder
	);

	/**
	 * Helpers and recipe information available while interpreting a slot display.
	 *
	 * @param <T> the type of ingredient being interpreted
	 *
	 * @since 30.20.0
	 */
	@ApiStatus.NonExtendable
	interface IContext<T> {
		/**
		 * Returns all ingredients of this type produced by the display.
		 * <p>
		 * JEI resolves these lazily when this method is called.
		 *
		 * @since 30.20.0
		 */
		@Unmodifiable
		List<ITypedIngredient<T>> getIngredients();

		/**
		 * Returns JEI's ingredient manager for working with registered ingredient types.
		 *
		 * @since 30.20.0
		 */
		IIngredientManager getIngredientManager();

		/**
		 * Returns the helper for the ingredient type being interpreted.
		 *
		 * @since 30.20.0
		 */
		IIngredientHelper<T> getIngredientHelper();

		/**
		 * Returns the same Minecraft context used to resolve the slot display.
		 *
		 * @since 30.20.0
		 */
		ContextMap getContextMap();

		/**
		 * Returns the role of the recipe slot.
		 * For example, an input may accept all subtypes while an output should remain exact.
		 *
		 * @since 30.20.0
		 */
		RecipeIngredientRole getRole();

		/**
		 * Resolves a display into values of this ingredient type using Minecraft's normal logic.
		 * This does not call JEI's slot display interpreters.
		 * It is useful for checking the contents of a display that is wrapped by the display being interpreted.
		 *
		 * @param slotDisplay the display to resolve
		 * @return the resolved values that match the ingredient type being interpreted
		 *
		 * @since 30.20.0
		 */
		Stream<T> resolve(SlotDisplay slotDisplay);
	}
}
