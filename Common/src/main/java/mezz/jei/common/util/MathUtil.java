package mezz.jei.common.util;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.phys.Vec2;
import net.minecraft.network.chat.FormattedText;

import javax.annotation.Nonnegative;

public final class MathUtil {
	@FunctionalInterface
	private interface Rectangle2dCropper {
		ImmutableRect2i crop(ImmutableRect2i original, ImmutableRect2i intersecting);
	}

	private static final List<Rectangle2dCropper> rectangle2dCroppers = List.of(
		MathUtil::cropTop,
		MathUtil::cropBottom,
		MathUtil::cropLeft,
		MathUtil::cropRight
	);

	private MathUtil() {

	}

	@SuppressWarnings("NumericCastThatLosesPrecision")
	public static int divideCeil(int numerator, int denominator) {
		return (int) Math.ceil((float) numerator / denominator);
	}

	public static boolean intersects(Collection<ImmutableRect2i> areas, ImmutableRect2i comparisonArea) {
		for (ImmutableRect2i area : areas) {
			if (intersects(area, comparisonArea)) {
				return true;
			}
		}
		return false;
	}

	public static boolean intersects(ImmutableRect2i rect1, ImmutableRect2i rect2) {
		if (rect1.isEmpty() || rect2.isEmpty()) {
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
	public static ImmutableRect2i cropToAvoidIntersection(Collection<ImmutableRect2i> areas, ImmutableRect2i comparisonArea, int availableWidth, int availableHeight) {
		final int maxWidth = Math.min(comparisonArea.getWidth(), availableWidth);
		final int maxHeight = Math.min(comparisonArea.getHeight(), availableHeight);

		return areas.stream()
			.filter(rectangle2d -> intersects(rectangle2d, comparisonArea))
			.sorted(Comparator.comparingInt(r -> contentArea(r, maxWidth, maxHeight)))
			.reduce(comparisonArea, (r1, r2) -> bestCrop(r1, r2, maxWidth, maxHeight));
	}

	/**
	 * Crop the given "rect" to avoid "intersecting" while maximizing the available content space.
	 */
	private static ImmutableRect2i bestCrop(ImmutableRect2i rect, ImmutableRect2i intersecting, int maxWidth, int maxHeight) {
		if (rect.isEmpty() || maxHeight == 0 || maxWidth == 0) {
			return rect;
		}
		return rectangle2dCroppers.stream()
			.map(cropper -> cropper.crop(rect, intersecting))
			.max(Comparator.comparingInt(r -> contentArea(r, maxWidth, maxHeight)))
			.orElse(ImmutableRect2i.EMPTY);
	}

	/**
	 * Calculates the area of flexible content that can fit in a given rect.
	 */
	@Nonnegative
	public static int contentArea(ImmutableRect2i rect, int maxWidth, int maxHeight) {
		return Math.min(rect.getWidth(), maxWidth) * Math.min(rect.getHeight(), maxHeight);
	}

	private static ImmutableRect2i cropTop(ImmutableRect2i original, ImmutableRect2i intersecting) {
		int newY = intersecting.getY() + intersecting.getHeight();
		int cropTopAmount = newY - original.getY();
		if (cropTopAmount < 0) {
			return ImmutableRect2i.EMPTY;
		}
		return original.cropTop(cropTopAmount);
	}

	private static ImmutableRect2i cropLeft(ImmutableRect2i original, ImmutableRect2i intersecting) {
		int newX = intersecting.getX() + intersecting.getWidth();
		int cropLeftAmount = newX - original.getX();
		if (cropLeftAmount < 0) {
			return ImmutableRect2i.EMPTY;
		}
		return original.cropLeft(cropLeftAmount);
	}

	private static ImmutableRect2i cropBottom(ImmutableRect2i original, ImmutableRect2i intersecting) {
		int newHeight = intersecting.getY() - original.getY();
		if (newHeight < 0) {
			return ImmutableRect2i.EMPTY;
		}
		return original.keepTop(newHeight);
	}

	private static ImmutableRect2i cropRight(ImmutableRect2i original, ImmutableRect2i intersecting) {
		int newWidth = intersecting.getX() - original.getX();
		if (newWidth < 0) {
			return ImmutableRect2i.EMPTY;
		}
		return original.keepLeft(newWidth);
	}

	public static boolean contains(Collection<ImmutableRect2i> areas, double x, double y) {
		for (ImmutableRect2i guiArea : areas) {
			if (guiArea.contains(x, y)) {
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

	public static ImmutableRect2i union(ImmutableRect2i rect1, ImmutableRect2i rect2) {
		if (rect1.isEmpty()) {
			return rect2;
		}
		if (rect2.isEmpty()) {
			return rect1;
		}
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
		return new ImmutableRect2i(tx1, ty1, (int) tx2, (int) ty2);
	}

	public static ImmutableRect2i centerTextArea(ImmutableRect2i outer, Font fontRenderer, String text) {
		int width = fontRenderer.width(text);
		int height = fontRenderer.lineHeight;
		return centerArea(outer, width, height);
	}

	public static ImmutableRect2i centerTextArea(ImmutableRect2i outer, Font fontRenderer, FormattedText text) {
		int width = fontRenderer.width(text);
		int height = fontRenderer.lineHeight;
		return centerArea(outer, width, height);
	}

	public static ImmutableRect2i centerArea(ImmutableRect2i outer, int width, int height) {
		return new ImmutableRect2i(
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
}
