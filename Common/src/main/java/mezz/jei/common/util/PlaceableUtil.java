package mezz.jei.common.util;

import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.placement.IPlaceable;
import mezz.jei.api.gui.placement.VerticalAlignment;

public final class PlaceableUtil {
	private PlaceableUtil() {
	}

	public static <T extends IPlaceable<T>> T setPosition(
		IPlaceable<T> placeable,
		int areaX,
		int areaY,
		int areaWidth,
		int areaHeight,
		HorizontalAlignment horizontalAlignment,
		VerticalAlignment verticalAlignment
	) {
		int xPos = areaX + horizontalAlignment.getXPos(areaWidth, placeable.getWidth());
		int yPos = areaY + verticalAlignment.getYPos(areaHeight, placeable.getHeight());
		return placeable.setPosition(xPos, yPos);
	}
}
