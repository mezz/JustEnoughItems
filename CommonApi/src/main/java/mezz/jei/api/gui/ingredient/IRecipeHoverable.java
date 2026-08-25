package mezz.jei.api.gui.ingredient;

/**
 * An element in a recipe layout that can report whether the mouse is over it.
 *
 * <p>Coordinates are relative to the recipe layout area, matching JEI's own recipe slots.
 * Implementations are free to apply their own transformations, clipping, or non-rectangular hit testing.</p>
 *
 * @since 30.26.0
 */
public interface IRecipeHoverable {
	/**
	 * Return true if the given mouse position is over this element.
	 *
	 * @param mouseX the x coordinate of the mouse, relative to the recipe layout area.
	 * @param mouseY the y coordinate of the mouse, relative to the recipe layout area.
	 *
	 * @since 30.26.0
	 */
	boolean isMouseOver(double mouseX, double mouseY);
}
