/**
 * Keyboard and mouse input for JEI controls.
 *
 * <p>Controls accept {@link mezz.jei.common.input.UserInput} events through
 * {@link mezz.jei.common.input.IInputTarget}.
 * {@link mezz.jei.common.input.interaction.InputAction} represents a single action,
 * such as showing recipes. {@link mezz.jei.common.input.interaction.MouseDrag} handles
 * a drag until release or cancellation.</p>
 *
 * <p>Mouse input stays with the interaction chosen on press. For example, a player
 * dragging a scrollbar can move the pointer outside it and keep scrolling until
 * releasing the button. Keyboard actions run immediately.</p>
 */
@NullMarked
package mezz.jei.common.input;

import org.jspecify.annotations.NullMarked;
