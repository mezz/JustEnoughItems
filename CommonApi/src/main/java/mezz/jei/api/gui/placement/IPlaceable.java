package mezz.jei.api.gui.placement;

/**
 * Interface for things that can have their position set, and be aligned vertically and horizontally in an area.
 *
 * @since 19.19.1
 */
public interface IPlaceable<THIS extends IPlaceable<THIS>> {
	/**
	 * Place this element at the given position.
	 * @since 19.19.1
	 */
	THIS setPosition(int xPos, int yPos);

	/**
	 * Place this element inside the given area, with the given alignment.
	 *
	 * @since 19.19.1
	 */
	default THIS setPosition(int areaX, int areaY, int areaWidth, int areaHeight, HorizontalAlignment horizontalAlignment, VerticalAlignment verticalAlignment) {
		int x = areaX + horizontalAlignment.getXPos(areaWidth, getWidth());
		int y = areaY + verticalAlignment.getYPos(areaHeight, getHeight());
		return setPosition(x, y);
	}

	/**
	 * Get the width of this element.
	 * @since 19.19.1
	 */
	int getWidth();

	/**
	 * Get the height of this element.
	 * @since 19.19.1
	 */
	int getHeight();
}
