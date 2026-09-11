package mezz.jei.api.gui.inputs;

import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import net.minecraft.client.gui.navigation.ScreenPosition;

/**
 * Represents a recipe slot currently under the mouse.
 *
 * Slots are positioned relative to their parent element, so they are not aware of their absolute position.
 * This class includes an offset to help determine where the slot is, relative to the caller, when getting a slot under the mouse.
 *
 * @param slot the slot under the mouse
 * @param offset the offset for this slot, relative to the caller
 *
 * @since 19.6.0
 */
public record RecipeSlotUnderMouse(IRecipeSlotDrawable slot, ScreenPosition offset) {
	/**
	 * Convenience function to create a new {@link RecipeSlotUnderMouse} with the given integer offsets.
	 *
	 * @since 19.6.0
	 */
	public RecipeSlotUnderMouse(IRecipeSlotDrawable slot, int xOffset, int yOffset) {
		this(slot, new ScreenPosition(xOffset, yOffset));
	}

	/**
	 * Convenience function to create a new {@link RecipeSlotUnderMouse} by adding the given integer offsets.
	 * This is useful when passing slots up a stack of nested widgets.
	 *
	 * @since 19.6.0
	 */
	public RecipeSlotUnderMouse addOffset(int xOffset, int yOffset) {
		return new RecipeSlotUnderMouse(slot, this.offset.x() + xOffset, this.offset.y() + yOffset);
	}

	/**
	 * Check if the mouse is still over this slot, from the perspective of the caller.
	 *
	 * @since 19.6.0
	 */
	public boolean isMouseOver(double mouseX, double mouseY) {
		double relativeMouseX = mouseX - offset.x();
		double relativeMouseY = mouseY - offset.y();
		return slot.isMouseOver(relativeMouseX, relativeMouseY);
	}
}
