package mezz.jei.api.gui.placement;

import org.jetbrains.annotations.ApiStatus;

/**
 * Interface for things that can have their position set, and be aligned vertically and horizontally in an area.
 *
 * @since 15.20.0
 */
@ApiStatus.NonExtendable
public interface IPlaceable<THIS extends IPlaceable<THIS>> {
	/**
	 * Place this element at the given position.
	 * @since 15.20.0
	 */
	THIS setPosition(int xPos, int yPos);

	/**
	 * Place this element inside the given area, with the given alignment.
	 *
	 * @since 15.20.0
	 */
	THIS setPosition(int areaX, int areaY, int areaWidth, int areaHeight, HorizontalAlignment horizontalAlignment, VerticalAlignment verticalAlignment);

	/**
	 * Get the width of this element.
	 * @since 15.20.0
	 */
	int getWidth();

	/**
	 * Get the height of this element.
	 * @since 15.20.0
	 */
	int getHeight();
}
