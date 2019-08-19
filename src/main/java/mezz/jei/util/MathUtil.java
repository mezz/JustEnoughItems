package mezz.jei.util;

import java.awt.Rectangle;
import java.util.Collection;

public final class MathUtil {
	private MathUtil() {

	}

	@SuppressWarnings("NumericCastThatLosesPrecision")
	public static int divideCeil(int numerator, int denominator) {
		return (int) Math.ceil((float) numerator / denominator);
	}

	public static int clamp(int value, int min, int max) {
		if (value < min) {
			return min;
		}
		return Math.min(value, max);
	}

	public static boolean intersects(Collection<Rectangle> areas, Rectangle comparisonArea) {
		for (Rectangle area : areas) {
			if (area.intersects(comparisonArea)) {
				return true;
			}
		}
		return false;
	}

	public static Rectangle moveDownToAvoidIntersection(Collection<Rectangle> areas, Rectangle comparisonArea) {
		for (Rectangle area : areas) {
			if (area.intersects(comparisonArea)) {
				Rectangle movedDown = new Rectangle(comparisonArea);
				movedDown.y = area.y + area.height;
				return moveDownToAvoidIntersection(areas, movedDown);
			}
		}
		return comparisonArea;
	}

	public static boolean contains(Collection<Rectangle> areas, int x, int y) {
		for (Rectangle guiArea : areas) {
			if (guiArea.contains(x, y)) {
				return true;
			}
		}
		return false;
	}
}
