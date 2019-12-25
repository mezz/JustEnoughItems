package mezz.jei.util;

import java.util.Collection;

import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.util.math.Vec2f;

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

	public static boolean intersects(Collection<Rectangle2d> areas, Rectangle2d comparisonArea) {
		for (Rectangle2d area : areas) {
			if (intersects(area, comparisonArea)) {
				return true;
			}
		}
		return false;
	}

	public static boolean intersects(Rectangle2d rect1, Rectangle2d rect2) {
		if (rect1.getWidth() <= 0 || rect1.getHeight() <= 0) {
			return false;
		}
		return rect2.getX() + rect2.getWidth() > rect1.getX() &&
			rect2.getY() + rect2.getHeight() > rect1.getY() &&
			rect2.getX() < rect1.getX() + rect1.getWidth() &&
			rect2.getY() < rect1.getY() + rect1.getHeight();
	}

	public static Rectangle2d moveDownToAvoidIntersection(Collection<Rectangle2d> areas, Rectangle2d comparisonArea) {
		for (Rectangle2d area : areas) {
			if (intersects(area, comparisonArea)) {
				Rectangle2d movedDown = new Rectangle2d(
					comparisonArea.getX(),
					area.getY() + area.getHeight(),
					comparisonArea.getWidth(),
					comparisonArea.getHeight()
				);
				return moveDownToAvoidIntersection(areas, movedDown);
			}
		}
		return comparisonArea;
	}

	public static boolean contains(Collection<Rectangle2d> areas, double x, double y) {
		for (Rectangle2d guiArea : areas) {
			if (contains(guiArea, x, y)) {
				return true;
			}
		}
		return false;
	}

	public static boolean contains(Rectangle2d rect, double x, double y) {
		return x >= rect.getX() &&
			y >= rect.getY() &&
			x < rect.getX() + rect.getWidth() &&
			y < rect.getY() + rect.getHeight();
	}

	public static Rectangle2d union(Rectangle2d rect1, Rectangle2d rect2) {
		long tx2 = rect1.getWidth();
		long ty2 = rect1.getHeight();
		long rx2 = rect2.getWidth();
		long ry2 = rect2.getHeight();
		int tx1 = rect1.getX();
		int ty1 = rect1.getY();
		tx2 += tx1;
		ty2 += ty1;
		int rx1 = rect2.getX();
		int ry1 = rect2.getY();
		rx2 += rx1;
		ry2 += ry1;
		if (tx1 > rx1) tx1 = rx1;
		if (ty1 > ry1) ty1 = ry1;
		if (tx2 < rx2) tx2 = rx2;
		if (ty2 < ry2) ty2 = ry2;
		tx2 -= tx1;
		ty2 -= ty1;
		tx2 = Math.min(tx2, Integer.MAX_VALUE);
		ty2 = Math.min(ty2, Integer.MAX_VALUE);
		return new Rectangle2d(tx1, ty1, (int) tx2, (int) ty2);
	}

	public static double distance (Vec2f start, Vec2f end){
		double a = start.x - end.x;
		double b = start.y - end.y;
		return Math.sqrt(a * a + b * b);
	}

}
