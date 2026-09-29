package mezz.jei.common.input.interaction;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.common.input.UserInput;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Calls handlers for mouse movement, release, and cancellation during a drag.
 */
public final class MouseDrag implements IInputInteraction {
	/** Receives mouse movement, or null when the drag needs no movement callback. */
	private final @Nullable IMovement onDrag;
	/** Finishes the drag when the button is released, using the release position if needed. */
	private final Consumer<UserInput> onRelease;
	/** Cleans up the drag when its screen closes or the interaction is replaced. */
	private final Runnable onCancel;
	/** Keeps dragging if JEI receives a duplicate button-press event before release. */
	private final boolean keepOnRepeatedPress;

	private MouseDrag(@Nullable IMovement onDrag, Consumer<UserInput> onRelease, Runnable onCancel, boolean keepOnRepeatedPress) {
		this.onDrag = onDrag;
		this.onRelease = onRelease;
		this.onCancel = onCancel;
		this.keepOnRepeatedPress = keepOnRepeatedPress;
	}

	/**
	 * Handles a scrollbar or resize handle that updates as the mouse moves.
	 * Both release and cancellation stop the drag without undoing those changes.
	 *
	 * <p>For example, a player holds the left mouse button on a scrollbar and drags down.
	 * The list scrolls as they move the mouse, and stays at that position when they let go.</p>
	 *
	 * @param onDrag updates the control as the mouse moves
	 * @param onStop clears the control's drag state on release or cancellation
	 */
	public static MouseDrag forWidget(IMovement onDrag, Runnable onStop) {
		return new MouseDrag(onDrag, ignored -> onStop.run(), onStop, false);
	}

	/**
	 * Handles an ingredient drag that needs only release and cancellation callbacks.
	 * A duplicate button-press event keeps the original drag, as
	 * described in {@link IInputInteraction#keepsCaptureOnRepeatedPress()}.
	 *
	 * <p>For example, a player holds the left mouse button on a bookmark, drags it to
	 * another slot, then lets go to put it there. Closing the inventory before letting
	 * go puts the bookmark back in its original place.</p>
	 *
	 * @param onRelease tries to drop the ingredient or move its bookmark to the release position
	 * @param onCancel stops the drag without dropping the ingredient
	 */
	public static MouseDrag forIngredient(Consumer<UserInput> onRelease, Runnable onCancel) {
		return new MouseDrag(null, onRelease, onCancel, true);
	}

	/** Receives the mouse position and distance moved, as described in {@link IInputInteraction#drag}. */
	@FunctionalInterface
	public interface IMovement {
		void move(double mouseX, double mouseY, InputConstants.Key button, double dragX, double dragY);
	}

	@Override
	public void complete(UserInput input) {
		onRelease.accept(input);
	}

	@Override
	public void drag(double mouseX, double mouseY, InputConstants.Key button, double dragX, double dragY) {
		if (onDrag != null) {
			onDrag.move(mouseX, mouseY, button, dragX, dragY);
		}
	}

	@Override
	public boolean capturesMouseMovement() {
		return onDrag != null;
	}

	@Override
	public void cancel() {
		onCancel.run();
	}

	@Override
	public boolean keepsCaptureOnRepeatedPress() {
		return keepOnRepeatedPress;
	}
}
