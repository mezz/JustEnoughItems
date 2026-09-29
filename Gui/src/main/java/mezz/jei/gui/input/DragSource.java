package mezz.jei.gui.input;

import mezz.jei.common.input.UserInput;
import mezz.jei.common.input.interaction.IInputInteraction;
import net.minecraft.client.gui.screens.Screen;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;

/**
 * Starts an ingredient drag from an input area, such as the ingredient list or bookmark list.
 * Chooses the ingredient when the left mouse button is pressed. The returned interaction
 * handles the drag until release or cancellation, even if the mouse pointer leaves that input area.
 *
 * @param enabled whether this source's input area is currently available
 * @param start locates the ingredient at the press position and starts its drag;
 *              returns empty when that ingredient cannot be dragged
 */
public record DragSource(BooleanSupplier enabled, BiFunction<Screen, UserInput, Optional<IInputInteraction>> start) {
	public Optional<IInputInteraction> begin(Screen screen, UserInput input) {
		if (enabled.getAsBoolean()) {
			return start.apply(screen, input);
		}
		return Optional.empty();
	}
}
