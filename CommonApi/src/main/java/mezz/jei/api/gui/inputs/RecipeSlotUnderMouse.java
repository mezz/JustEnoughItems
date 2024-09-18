package mezz.jei.api.gui.inputs;

import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;

/**
 * Represents a recipe slot currently under the mouse.
 *
 * Slots are positioned relative to their parent element, so they are not aware of their absolute position.
 * This class includes an offset to help determine where the slot is, relative to the caller, when getting a slot under the mouse.
 *
 * @param slot the slot under the mouse
 * @param x the X offset for this slot, relative to the caller
 * @param y the Y offset for this slot, relative to the caller
 *
 * @since 11.7.0
 */
public record RecipeSlotUnderMouse(IRecipeSlotDrawable slot, int x, int y) {
	/**
	 * Convenience function to create a new {@link RecipeSlotUnderMouse} by adding the given integer offsets.
	 * This is useful when passing slots up a stack of nested widgets.
	 *
	 * @since 11.7.0
	 */
	public RecipeSlotUnderMouse addOffset(int xOffset, int yOffset) {
		return new RecipeSlotUnderMouse(slot, this.x() + xOffset, this.y() + yOffset);
	}

	/**
	 * Check if the mouse is still over this slot, from the perspective of the caller.
	 *
	 * @since 11.7.0
	 */
	public boolean isMouseOver(double mouseX, double mouseY) {
		double relativeMouseX = mouseX - x();
		double relativeMouseY = mouseY - y();
		return slot.isMouseOver(relativeMouseX, relativeMouseY);
	}
}
