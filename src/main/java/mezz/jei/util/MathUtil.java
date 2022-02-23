package mezz.jei.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.util.Tuple;
import net.minecraft.world.phys.Vec2;
import net.minecraft.network.chat.FormattedText;
import net.minecraftforge.common.util.Size2i;

public final class MathUtil {
	@FunctionalInterface
	private interface Rectangle2dCropper {
		Rect2i crop(Rect2i original, Rect2i intersecting);
	}

	private static final List<Rectangle2dCropper> rectangle2dCroppers = List.of(
		MathUtil::cropTop,
		MathUtil::cropBottom,
		MathUtil::cropLeft,
		MathUtil::cropRight
	);

	private static final Rect2i emptyRect = new Rect2i(0, 0, 0, 0);

	private MathUtil() {

	}

	@SuppressWarnings("NumericCastThatLosesPrecision")
	public static int divideCeil(int numerator, int denominator) {
		return (int) Math.ceil((float) numerator / denominator);
	}

	public static boolean intersects(Collection<Rect2i> areas, Rect2i comparisonArea) {
		for (Rect2i area : areas) {
			if (intersects(area, comparisonArea)) {
				return true;
			}
		}
		return false;
	}

	public static boolean intersects(Rect2i rect1, Rect2i rect2) {
		if (rect1.getWidth() <= 0 || rect1.getHeight() <= 0) {
			return false;
		}
		return rect2.getX() + rect2.getWidth() > rect1.getX() &&
			rect2.getY() + rect2.getHeight() > rect1.getY() &&
			rect2.getX() < rect1.getX() + rect1.getWidth() &&
			rect2.getY() < rect1.getY() + rect1.getHeight();
	}

	/**
	 * Tries cropping "comparisonArea" in 4 different directions to get out of the way of "areas".
	 * Returns the largest resulting area after the crop, to find the "best" way of getting out of the way.
	 */
	public static Rect2i cropToAvoidIntersection(Collection<Rect2i> areas, Rect2i comparisonArea, Size2i maxContentSize) {
		return areas.stream()
			.filter(rectangle2d -> intersects(rectangle2d, comparisonArea))
			.sorted(Comparator.comparingInt(r -> contentArea(r, maxContentSize)))
			.reduce(comparisonArea, (r1, r2) -> bestCrop(r1, r2, maxContentSize));
	}

	/**
	 * Crop the given "rect" to avoid "intersecting" while maximizing the available content space.
	 */
	private static Rect2i bestCrop(Rect2i rect, Rect2i intersecting, Size2i maxContentSize) {
		if (contentArea(rect, maxContentSize) == 0) {
			return rect;
		}
		return rectangle2dCroppers.stream()
			.map(cropper -> cropper.crop(rect, intersecting))
			.max(Comparator.comparingInt(r -> contentArea(r, maxContentSize)))
			.orElse(emptyRect);
	}

	/**
	 * Calculates the area of flexible content that can fit in a given rect.
	 */
	public static int contentArea(Rect2i rect, Size2i maxContentSize) {
		if (rect.getWidth() <= 0 || rect.getHeight() <= 0) {
			return 0;
		}
		return Math.min(rect.getWidth(), maxContentSize.width) * Math.min(rect.getHeight(), maxContentSize.height);
	}

	private static Rect2i cropTop(Rect2i original, Rect2i intersecting) {
		int maxY = original.getY() + original.getHeight();
		int newY = intersecting.getY() + intersecting.getHeight();
		if (maxY < newY) {
			return emptyRect;
		}
		return new Rect2i(
			original.getX(),
			newY,
			original.getWidth(),
			maxY - newY
		);
	}

	private static Rect2i cropBottom(Rect2i original, Rect2i intersecting) {
		int newHeight = intersecting.getY() - original.getY();
		if (newHeight < 0) {
			return emptyRect;
		}
		return new Rect2i(
			original.getX(),
			original.getY(),
			original.getWidth(),
			newHeight
		);
	}

	private static Rect2i cropRight(Rect2i original, Rect2i intersecting) {
		int newWidth = intersecting.getX() - original.getX();
		if (newWidth < 0) {
			return emptyRect;
		}
		return new Rect2i(
			original.getX(),
			original.getY(),
			newWidth,
			original.getHeight()
		);
	}

	private static Rect2i cropLeft(Rect2i original, Rect2i intersecting) {
		int maxX = original.getX() + original.getWidth();
		int newX = intersecting.getX() + intersecting.getWidth();
		if (maxX < newX) {
			return emptyRect;
		}
		return new Rect2i(
			newX,
			original.getY(),
			maxX - newX,
			original.getHeight()
		);
	}

	public static boolean contains(Collection<Rect2i> areas, double x, double y) {
		for (Rect2i guiArea : areas) {
			if (contains(guiArea, x, y)) {
				return true;
			}
		}
		return false;
	}

	public static boolean contains(Rect2i rect, double x, double y) {
		return x >= rect.getX() &&
			y >= rect.getY() &&
			x < rect.getX() + rect.getWidth() &&
			y < rect.getY() + rect.getHeight();
	}

	public static Rect2i union(Rect2i rect1, Rect2i rect2) {
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
        if (tx1 > rx1) {
            tx1 = rx1;
        }
        if (ty1 > ry1) {
            ty1 = ry1;
        }
        if (tx2 < rx2) {
            tx2 = rx2;
        }
        if (ty2 < ry2) {
            ty2 = ry2;
        }
		tx2 -= tx1;
		ty2 -= ty1;
		tx2 = Math.min(tx2, Integer.MAX_VALUE);
		ty2 = Math.min(ty2, Integer.MAX_VALUE);
		return new Rect2i(tx1, ty1, (int) tx2, (int) ty2);
	}

	public static Rect2i centerTextArea(Rect2i outer, Font fontRenderer, String text) {
		int width = fontRenderer.width(text);
		int height = fontRenderer.lineHeight;
		return centerArea(outer, width, height);
	}

	public static Rect2i centerTextArea(Rect2i outer, Font fontRenderer, FormattedText text) {
		int width = fontRenderer.width(text);
		int height = fontRenderer.lineHeight;
		return centerArea(outer, width, height);
	}

	public static Rect2i centerArea(Rect2i outer, int width, int height) {
		return new Rect2i(
			outer.getX() + Math.round((outer.getWidth() - width) / 2.0f),
			outer.getY() + Math.round((outer.getHeight() - height) / 2.0f),
			width,
			height
		);
	}

	public static double distance(Vec2 start, Vec2 end) {
		double a = start.x - end.x;
		double b = start.y - end.y;
		return Math.sqrt(a * a + b * b);
	}

	public static Tuple<Rect2i, Rect2i> splitY(Rect2i rectangle, int y) {
		Rect2i rectTop = new Rect2i(
			rectangle.getX(),
			rectangle.getY(),
			rectangle.getWidth(),
			y
		);
		Rect2i rectBottom = new Rect2i(
			rectangle.getX(),
			rectangle.getY() + y,
			rectangle.getWidth(),
			rectangle.getHeight() - y
		);
		return new Tuple<>(rectTop, rectBottom);
	}

	public static Tuple<Rect2i, Rect2i> splitYBottom(Rect2i rectangle, int y) {
		return splitY(rectangle, rectangle.getHeight() - y);
	}

	public static Tuple<Rect2i, Rect2i> splitX(Rect2i rectangle, int x) {
		Rect2i rectLeft = new Rect2i(
			rectangle.getX(),
			rectangle.getY(),
			x,
			rectangle.getHeight()
		);
		Rect2i rectRight = new Rect2i(
			rectangle.getX() + x,
			rectangle.getY(),
			rectangle.getWidth() - x,
			rectangle.getHeight()
		);
		return new Tuple<>(rectLeft, rectRight);
	}

	public static Tuple<Rect2i, Rect2i> splitXRight(Rect2i rectangle, int x) {
		return splitX(rectangle, rectangle.getWidth() - x);
	}

	public static boolean equalRects(Collection<Rect2i> a, Collection<Rect2i> b) {
		if (a.size() != b.size()) {
			return false;
		}
		for (Rect2i aRect : a) {
			if (!containsRect(b, aRect)) {
				return false;
			}
		}
		return true;
	}

	public static Rect2i copyRect(Rect2i rect) {
		return new Rect2i(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
	}

	private static boolean containsRect(Collection<Rect2i> rects, Rect2i aRect) {
		for (Rect2i bRect : rects) {
			if (aRect.getX() == bRect.getX() &&
				aRect.getY() == bRect.getY() &&
				aRect.getWidth() == bRect.getWidth() &&
				aRect.getHeight() == bRect.getHeight()) {
				return true;
			}
		}
		return false;
	}
}
