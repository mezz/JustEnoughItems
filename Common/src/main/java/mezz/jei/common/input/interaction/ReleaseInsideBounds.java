package mezz.jei.common.input.interaction;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.common.input.IMouseOverable;
import mezz.jei.common.input.UserInput;

/**
 * Finishes a click or drag only if the pointer is still inside the original control
 * or visible area. Mouse movement still reaches the interaction outside that area.
 * Releasing outside cancels the interaction.
 *
 * <p>For example, a player holds the left mouse button on a control in a recipe, moves
 * the mouse outside the visible recipe list, then lets go. The control's action does
 * not run, even if the recipe extends beyond the edge of the list.</p>
 *
 * @param interaction the interaction chosen on press, to complete or cancel
 * @param bounds the area the pointer must be inside when the interaction completes
 */
public record ReleaseInsideBounds(IInputInteraction interaction, IMouseOverable bounds) implements IInputInteraction {
	@Override
	public void complete(UserInput input) {
		if (bounds.isMouseOver(input.getMouseX(), input.getMouseY())) {
			interaction.complete(input);
		} else {
			interaction.cancel();
		}
	}

	@Override
	public void drag(double mouseX, double mouseY, InputConstants.Key button, double dragX, double dragY) {
		interaction.drag(mouseX, mouseY, button, dragX, dragY);
	}

	@Override
	public boolean capturesMouseMovement() {
		return interaction.capturesMouseMovement();
	}

	@Override
	public boolean keepsCaptureOnRepeatedPress() {
		return interaction.keepsCaptureOnRepeatedPress();
	}

	@Override
	public void cancel() {
		interaction.cancel();
	}
}
