package mezz.jei.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.text.ITextProperties;
import net.minecraftforge.common.util.Size2i;

public final class MathUtil {
	@FunctionalInterface
	private interface Rectangle2dCropper {
		Rectangle2d crop(Rectangle2d original, Rectangle2d intersecting);
	}

	private static final List<Rectangle2dCropper> rectangle2dCroppers = Arrays.asList(
		MathUtil::cropTop,
		MathUtil::cropBottom,
		MathUtil::cropLeft,
		MathUtil::cropRight
	);

	private static final Rectangle2d emptyRect = new Rectangle2d(0, 0, 0, 0);

	private MathUtil() {

	}

	@SuppressWarnings("NumericCastThatLosesPrecision")
	public static int divideCeil(int numerator, int denominator) {
		return (int) Math.ceil((float) numerator / denominator);
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

	/**
	 * Tries cropping "comparisonArea" in 4 different directions to get out of the way of "areas".
	 * Returns the largest resulting area after the crop, to find the "best" way of getting out of the way.
	 */
	public static Rectangle2d cropToAvoidIntersection(Collection<Rectangle2d> areas, Rectangle2d comparisonArea, Size2i maxContentSize) {
		return areas.stream()
			.filter(rectangle2d -> intersects(rectangle2d, comparisonArea))
			.sorted(Comparator.comparingInt(r -> contentArea(r, maxContentSize)))
			.reduce(comparisonArea, (r1, r2) -> bestCrop(r1, r2, maxContentSize));
	}

	/**
	 * Crop the given "rect" to avoid "intersecting" while maximizing the available content space.
	 */
	private static Rectangle2d bestCrop(Rectangle2d rect, Rectangle2d intersecting, Size2i maxContentSize) {
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
	public static int contentArea(Rectangle2d rect, Size2i maxContentSize) {
		if (rect.getWidth() <= 0 || rect.getHeight() <= 0) {
			return 0;
		}
		return Math.min(rect.getWidth(), maxContentSize.width) * Math.min(rect.getHeight(), maxContentSize.height);
	}

	private static Rectangle2d cropTop(Rectangle2d original, Rectangle2d intersecting) {
		int maxY = original.getY() + original.getHeight();
		int newY = intersecting.getY() + intersecting.getHeight();
		if (maxY < newY) {
			return emptyRect;
		}
		return new Rectangle2d(
			original.getX(),
			newY,
			original.getWidth(),
			maxY - newY
		);
	}

	private static Rectangle2d cropBottom(Rectangle2d original, Rectangle2d intersecting) {
		int newHeight = intersecting.getY() - original.getY();
		if (newHeight < 0) {
			return emptyRect;
		}
		return new Rectangle2d(
			original.getX(),
			original.getY(),
			original.getWidth(),
			newHeight
		);
	}

	private static Rectangle2d cropRight(Rectangle2d original, Rectangle2d intersecting) {
		int newWidth = intersecting.getX() - original.getX();
		if (newWidth < 0) {
			return emptyRect;
		}
		return new Rectangle2d(
			original.getX(),
			original.getY(),
			newWidth,
			original.getHeight()
		);
	}

	private static Rectangle2d cropLeft(Rectangle2d original, Rectangle2d intersecting) {
		int maxX = original.getX() + original.getWidth();
		int newX = intersecting.getX() + intersecting.getWidth();
		if (maxX < newX) {
			return emptyRect;
		}
		return new Rectangle2d(
			newX,
			original.getY(),
			maxX - newX,
			original.getHeight()
		);
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
		return new Rectangle2d(tx1, ty1, (int) tx2, (int) ty2);
	}

	public static Rectangle2d centerTextArea(Rectangle2d outer, FontRenderer fontRenderer, String text) {
		int width = fontRenderer.getStringWidth(text);
		int height = fontRenderer.FONT_HEIGHT;
		return centerArea(outer, width, height);
	}

	public static Rectangle2d centerTextArea(Rectangle2d outer, FontRenderer fontRenderer, ITextProperties text) {
		int width = fontRenderer.getStringPropertyWidth(text);
		int height = fontRenderer.FONT_HEIGHT;
		return centerArea(outer, width, height);
	}

	public static Rectangle2d centerArea(Rectangle2d outer, int width, int height) {
		return new Rectangle2d(
			outer.getX() + Math.round((outer.getWidth() - width) / 2.0f),
			outer.getY() + Math.round((outer.getHeight() - height) / 2.0f),
			width,
			height
		);
	}

	public static double distance(Vector2f start, Vector2f end) {
		double a = start.x - end.x;
		double b = start.y - end.y;
		return Math.sqrt(a * a + b * b);
	}

	public static Tuple<Rectangle2d, Rectangle2d> splitY(Rectangle2d rectangle, int y) {
		Rectangle2d rectTop = new Rectangle2d(
			rectangle.getX(),
			rectangle.getY(),
			rectangle.getWidth(),
			y
		);
		Rectangle2d rectBottom = new Rectangle2d(
			rectangle.getX(),
			rectangle.getY() + y,
			rectangle.getWidth(),
			rectangle.getHeight() - y
		);
		return new Tuple<>(rectTop, rectBottom);
	}

	public static Tuple<Rectangle2d, Rectangle2d> splitYBottom(Rectangle2d rectangle, int y) {
		return splitY(rectangle, rectangle.getHeight() - y);
	}

	public static Tuple<Rectangle2d, Rectangle2d> splitX(Rectangle2d rectangle, int x) {
		Rectangle2d rectLeft = new Rectangle2d(
			rectangle.getX(),
			rectangle.getY(),
			x,
			rectangle.getHeight()
		);
		Rectangle2d rectRight = new Rectangle2d(
			rectangle.getX() + x,
			rectangle.getY(),
			rectangle.getWidth() - x,
			rectangle.getHeight()
		);
		return new Tuple<>(rectLeft, rectRight);
	}

	public static Tuple<Rectangle2d, Rectangle2d> splitXRight(Rectangle2d rectangle, int x) {
		return splitX(rectangle, rectangle.getWidth() - x);
	}

	public static boolean equalRects(Collection<Rectangle2d> a, Collection<Rectangle2d> b) {
		if (a.size() != b.size()) {
			return false;
		}
		for (Rectangle2d aRect : a) {
			if (!containsRect(b, aRect)) {
				return false;
			}
		}
		return true;
	}

	private static boolean containsRect(Collection<Rectangle2d> rects, Rectangle2d aRect) {
		for (Rectangle2d bRect : rects) {
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
